package com.afeka.liadk.iplay.UserProfile;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;

import com.afeka.liadk.iplay.MainActivity;
import com.afeka.liadk.iplay.R;
import com.afeka.liadk.iplay.Tournament.TournamentActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class UserImageProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUSET = 1;

    private Button mNext, mCancel;
    private CircleImageView mImageHolder;
    private boolean mImageHasChange;
    private Uri mImageUri;
    private StorageReference mStorageRef;

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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(getContext()).load(MainActivity.CurrentUser.getPhotoUrl()).into(mImageHolder);
                    }
                });
            }
        } else {
            MainActivity.checkPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    R.string.read_permission, MainActivity.REQUEST_CODE_READ);
        }
        mStorageRef = FirebaseStorage.getInstance().getReference("users");
        return view;
    }

    private void picChoose() {
        if (MainActivity.checkPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE,
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

        }
        cancel();
    }
}
