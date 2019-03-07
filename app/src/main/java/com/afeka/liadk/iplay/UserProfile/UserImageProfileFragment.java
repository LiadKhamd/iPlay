package com.afeka.liadk.iplay.UserProfile;
/*
 *Created by liadk
 */

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.afeka.liadk.iplay.FireBaseConst;
import com.afeka.liadk.iplay.R;
import com.afeka.liadk.iplay.Tournament.TournamentActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class UserImageProfileFragment extends Fragment implements FireBaseConst {

    private final int REQUEST_CODE_READ = 102;
    private final int REQUEST_CODE_WRITE = 103;

    private static final int PICK_IMAGE_REQUSET = 1;

    private Button mNext, mCancel;
    private CircleImageView mImageHolder;
    private boolean mImageHasChange;
    private Uri mImageUri;
    private StorageReference mStorageRef;
    private ProgressDialog mProgressDialog;
    private CollectionReference mCollectionReference;
    private FirebaseUser mCurrentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_image_profile, container, false);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
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
                choosePic();
            }
        });
        mImageHasChange = false;
        mImageUri = mCurrentUser.getPhotoUrl();
        if (mImageUri != null)  //There is a profile image
            setImage();
        mStorageRef = FirebaseStorage.getInstance().getReference(USERS);
        mProgressDialog = new ProgressDialog(getContext(), R.style.ProgressDialogTheme);
        mProgressDialog.setTitle(R.string.update_image_profile);
        mProgressDialog.setMessage(getContext().getString(R.string.please_wait_update_proflie));
        mProgressDialog.setCancelable(false);
        mCollectionReference = FirebaseFirestore.getInstance().collection(USERS);
        return view;
    }

    private void setImage() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme).setTitle(R.string.storage_access)
                    .setMessage(R.string.write_permission)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestPermissions(
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_CODE_WRITE
                            );
                        }
                    }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(getContext()).load(mImageUri).into(mImageHolder);
                }
            });
        }
    }

    private void choosePic() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme).setTitle(R.string.storage_access)
                    .setMessage(R.string.read_permission)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestPermissions(
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_CODE_READ
                            );
                        }
                    }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE_REQUSET);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {//Picture has been chosen
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUSET && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            mImageHasChange = true;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(getContext()).load(mImageUri).into(mImageHolder);
                }
            });
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

    private void upload() {//Upload the image and set the url to current user data
        mProgressDialog.show();
        mStorageRef.child(mCurrentUser.getUid() + "." + getFileExtension(mImageUri))
                .putFile(mImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mStorageRef.child(mCurrentUser.getUid() + "." + getFileExtension(mImageUri))
                                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri uri) {
                                Map<String, Object> updatesMe = new HashMap<>();
                                updatesMe.put("mUriImage", uri.toString());
                                mCollectionReference.document(mCurrentUser.getDisplayName())
                                        .update(updatesMe).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setPhotoUri(uri)
                                                .build();
                                        mCurrentUser.updateProfile(profileUpdates)
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
                } catch (Exception e) {
                    Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_READ: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    choosePic();
                }
                return;
            }
            case REQUEST_CODE_WRITE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    setImage();
                }
                return;
            }
        }
    }
}
