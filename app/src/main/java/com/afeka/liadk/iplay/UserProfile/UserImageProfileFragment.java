package com.afeka.liadk.iplay.UserProfile;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.afeka.liadk.iplay.FireBaseConst;
import com.afeka.liadk.iplay.MainActivity;
import com.afeka.liadk.iplay.R;
import com.afeka.liadk.iplay.Tournament.TournamentActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class UserImageProfileFragment extends Fragment implements FireBaseConst, MainActivity.ImageReq {

    private static final int PICK_IMAGE_REQUSET = 1;

    private Button mNext, mCancel;
    private CircleImageView mImageHolder;
    private boolean mImageHasChange;
    private Uri mImageUri;
    private StorageReference mStorageRef;
    private ProgressDialog mProgressDialog;
    private CollectionReference mCollectionReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_image_profile, container, false);
        mNext = view.findViewById(R.id.next_profile_image_button);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAndContinue();
            }
        });
        mCancel = view.findViewById(R.id.cancel_profile_image_button);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
        mImageHolder = view.findViewById(R.id.profile_holder);
        mImageHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picChoose();
            }
        });
        mImageHasChange = false;

        if (MainActivity.CurrentUser.getPhotoUrl() != null) {
            if (MainActivity.checkPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    R.string.write_permission, MainActivity.REQUEST_CODE_WRITE)) {
                mImageUri = MainActivity.CurrentUser.getPhotoUrl();
                setImage();
            }
        } else {
            MainActivity.checkPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    R.string.read_permission, MainActivity.REQUEST_CODE_READ);
        }
        mStorageRef = FirebaseStorage.getInstance().getReference(USERS);
        mProgressDialog = new ProgressDialog(getContext(), R.style.ProgressDialogTheme);
        mProgressDialog.setMessage(getContext().getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);
        mCollectionReference = FirebaseFirestore.getInstance().collection(USERS);
        return view;
    }

    private void setImage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(getContext()).load(mImageUri).into(mImageHolder);
            }
        });
    }

    private void picChoose() {
        if (MainActivity.checkPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE,
                R.string.read_permission, MainActivity.REQUEST_CODE_READ)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE_REQUSET);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUSET && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            mImageHasChange = true;
            setImage();
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void cancel() {
        Intent menuIntent = new Intent(getContext(), TournamentActivity.class);
        menuIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(menuIntent);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        getActivity().finish();
    }

    private void saveAndContinue() {
        if (mImageHasChange) {
            upload();
        } else
            cancel();
    }

    private void upload() {
        mProgressDialog.show();
        mStorageRef.child(MainActivity.CurrentUser.getUid() + "." + getFileExtension(mImageUri))
                .putFile(mImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mStorageRef.child(MainActivity.CurrentUser.getUid() + "." + getFileExtension(mImageUri))
                                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri uri) {
                                Map<String, Object> updatesMe = new HashMap<>();
                                updatesMe.put("mUriImage", uri.toString());
                                mCollectionReference.document(MainActivity.CurrentUser.getDisplayName())
                                        .update(updatesMe).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setPhotoUri(uri)
                                                .build();
                                        MainActivity.CurrentUser.updateProfile(profileUpdates)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getContext(), R.string.update_done, Toast.LENGTH_LONG).show();
                                                        cancel();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception ex) {
                                                mProgressDialog.cancel();
                                                try {
                                                    throw ex;
                                                } catch (FirebaseNetworkException e) {
                                                    Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                                                } catch (FirebaseFirestoreException e) {
                                                    Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                                                } catch (Exception e) {
                                                    Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception ex) {
                                        mProgressDialog.cancel();
                                        try {
                                            throw ex;
                                        } catch (FirebaseNetworkException e) {
                                            Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                                        } catch (FirebaseFirestoreException e) {
                                            Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                                        } catch (Exception e) {
                                            Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception ex) {
                mProgressDialog.cancel();
                try {
                    throw ex;
                } catch (FirebaseNetworkException e) {
                    Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                } catch (FirebaseFirestoreException e) {
                    Toast.makeText(getContext(), R.string.network_problem, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void iGetPermissionToRead() {
        picChoose();
    }

    @Override
    public void iGetPermissionToWrite() {
        setImage();
    }
}
