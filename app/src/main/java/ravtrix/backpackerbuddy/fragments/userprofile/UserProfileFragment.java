package ravtrix.backpackerbuddy.fragments.userprofile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import ravtrix.backpackerbuddy.R;
import ravtrix.backpackerbuddy.activities.editinfo.EditInfoActivity;
import ravtrix.backpackerbuddy.activities.editphoto.EditPhotoActivity;
import ravtrix.backpackerbuddy.activities.usermap.UserMapActivity;
import ravtrix.backpackerbuddy.helpers.Helpers;
import ravtrix.backpackerbuddy.interfacescom.FragActivityUpdateProfilePic;
import ravtrix.backpackerbuddy.models.UserLocalStore;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Ravinder on 8/18/16.
 */
public class UserProfileFragment extends Fragment implements View.OnClickListener, IUserProfileView {

    @BindView(R.id.profile_image_profile) protected CircleImageView profilePic;
    @BindView(R.id.frag_user_profile_etLayout1) protected LinearLayout editLayout;
    @BindView(R.id.frag_user_profile_etLayout2) protected LinearLayout editLayout2;
    @BindView(R.id.frag_user_profile_etLayout3) protected LinearLayout editLayout3;
    @BindView(R.id.frag_user_profile_etLayout4) protected LinearLayout editLayout4;
    @BindView(R.id.ll_edit) protected LinearLayout editLayoutSub1;
    @BindView(R.id.ll_edit2) protected LinearLayout editLayoutSub2;
    @BindView(R.id.ll_edit3) protected LinearLayout editLayoutSub3;
    @BindView(R.id.ll_edit4) protected LinearLayout editLayoutSub4;
    @BindView(R.id.title1) protected TextView title1;
    @BindView(R.id.title2) protected TextView title2;
    @BindView(R.id.title3) protected TextView title3;
    @BindView(R.id.title4) protected TextView title4;
    @BindView(R.id.hint1) protected TextView detailOne;
    @BindView(R.id.hint2) protected TextView detailTwo;
    @BindView(R.id.hint3) protected TextView detailThree;
    @BindView(R.id.hint4) protected TextView detailFour;
    @BindView(R.id.username_profile) protected TextView username;
    @BindView(R.id.imgbEditPhoto) protected ImageButton imgbEditPhoto;
    @BindView(R.id.user_profile_tvLocation) protected TextView tvLocation;
    @BindView(R.id.imgTravel) protected ImageView imgTravel;
    @BindView(R.id.imgNotTravel) protected ImageView imgNotTravel;
    @BindView(R.id.txtTravel) protected TextView txtTravel;
    @BindView(R.id.txtNotTravel) protected TextView txtNotTravel;
    @BindView(R.id.imgTravelStatusEdit) protected ImageView imgTravelStatusEdit;
    @BindView(R.id.relativeFragProfile) protected RelativeLayout relativeFragProfile;
    @BindView(R.id.frag_profile_progressBar) protected ProgressBar progressBar;

    private UserLocalStore userLocalStore;
    private View v;
    private FragActivityUpdateProfilePic fragActivityUpdateProfilePic;
    private boolean isDetailOneAHint = true;
    private boolean isDetailTwoAHint = true;
    private boolean isDetailThreeAHint = true;
    private boolean isDetailFourAHint = true;
    private UserProfilePresenter presenter;
    private ProgressDialog progressDialog;
    private boolean refreshPage = false;
    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        this.fragActivityUpdateProfilePic = (FragActivityUpdateProfilePic) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_user_profile, container, false);
        ButterKnife.bind(this, v);
        relativeFragProfile.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        userLocalStore = new UserLocalStore(getActivity());
        presenter = new UserProfilePresenter(this);

        setProfilePic();
        Helpers.overrideFonts(getActivity(), relativeFragProfile);
        setViewListeners();
        //checkLocationUpdate();

        presenter.getUserInfo(userLocalStore.getLoggedInUser().getUserID(),
                userLocalStore.getLoggedInUser().getUserImageURL());
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.frag_user_profile_etLayout1:
            case R.id.ll_edit:
                setIntentEditInto(title1.getText().toString(), detailOne.getText().toString(), "1");
                break;
            case R.id.frag_user_profile_etLayout2:
            case R.id.ll_edit2:
                setIntentEditInto(title2.getText().toString(), detailTwo.getText().toString(), "2");
                break;
            case R.id.frag_user_profile_etLayout3:
            case R.id.ll_edit3:
                setIntentEditInto(title3.getText().toString(), detailThree.getText().toString(), "3");
                break;
            case R.id.frag_user_profile_etLayout4:
            case R.id.ll_edit4:
                setIntentEditInto(title4.getText().toString(), detailFour.getText().toString(), "4");
                break;
            case R.id.imgbEditPhoto:
                startActivityForResult(new Intent(getActivity(), EditPhotoActivity.class), 1);
                break;
            case R.id.imgTravelStatusEdit:
                AlertDialog.Builder alertDialog = Helpers.showAlertDialogWithTwoOptions(getActivity(), "Travel Status",
                        "Change your traveling status?", "No");
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressDialog = Helpers.showProgressDialog(getContext(), "Updating...");

                        final HashMap<String, String> userInfo = new HashMap<>();

                        userInfo.put("userID", Integer.toString(userLocalStore.getLoggedInUser().getUserID()));
                        presenter.updateTravelStatus(userInfo, userLocalStore);
                    }
                });
                alertDialog.show();
                break;
            default:
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // blow up the world menu on toolbar
        inflater.inflate(R.menu.toolbar_map, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.optionButtonMap:
                startActivity(new Intent(getContext(), UserMapActivity.class));
                return super.onOptionsItemSelected(item);
            default:
                return false;
        }
    }

    // Pass title and hint to edit info activity based on edit selection type
    private void setIntentEditInto(String title, String detail, String detailType) {
        Intent intent = new Intent(context, EditInfoActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("detail", detail);
        intent.putExtra("detailType", detailType); // Type of detail to know which column in the database to insert
        boolean isAHint = true;

        // Pass isHint extra because default description should be displayed as hint in the EditInfoActivity
        // Non-default (user edited before) should be displayed in EditText as text not hint
        switch (detailType) {
            case "1":
                isAHint = isDetailOneAHint;
                break;
            case "2":
                isAHint = isDetailTwoAHint;
                break;
            case "3":
                isAHint = isDetailThreeAHint;
                break;
            case "4":
                isAHint = isDetailFourAHint;
                break;
            default:
        }
        intent.putExtra("isHint", isAHint);
        startActivityForResult(intent, 3);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data.getBooleanExtra("refresh", true)) { // only refresh if new photo set
            Picasso.with(getContext())
                    .load(userLocalStore.getLoggedInUser().getUserImageURL())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .fit()
                    .centerCrop()
                    .into(profilePic);
            v.setVisibility(View.VISIBLE);

            // Also notify navigation drawer to change its profile picture by invoking the main activity
            fragActivityUpdateProfilePic.onUpdateProfilePic();
        } else if (requestCode == 3) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            UserProfileFragment fragment = new UserProfileFragment();
            fragmentManager.beginTransaction().replace(R.id.fragment_container,
                    fragment).commit();
            fragment.refreshPage = true;
        }
    }

    private void setViewListeners() {
        editLayout.setOnClickListener(this);
        editLayout2.setOnClickListener(this);
        editLayout3.setOnClickListener(this);
        editLayout4.setOnClickListener(this);
        imgbEditPhoto.setOnClickListener(this);
        editLayoutSub1.setOnClickListener(this);
        editLayoutSub2.setOnClickListener(this);
        editLayoutSub3.setOnClickListener(this);
        editLayoutSub4.setOnClickListener(this);
        imgbEditPhoto.setOnClickListener(this);
        imgTravelStatusEdit.setOnClickListener(this);
    }

    @Override
    public void setUserLocation(String country) {
        tvLocation.setText(country);
        setViewVisible();
        hideProgressBar();
    }

    @Override
    public void setUsername(String username) {
        this.username.setText(username);
        setViewVisible();
        hideProgressBar();
    }
    @Override
    public void setDetailOneText(String text) {
        detailOne.setText(text);
    }
    @Override
    public void setDetailOneHint(String hint) {
        detailOne.setHint(hint);
    }
    @Override
    public void setDetailTwoText(String text) {
        detailTwo.setText(text);
    }
    @Override
    public void setDetailTwoHint(String hint) {
        detailTwo.setHint(hint);
    }
    @Override
    public void setDetailThreeText(String text) {
        detailThree.setText(text);
    }
    @Override
    public void setDetailThreeHint(String hint) {
        detailThree.setHint(hint);
    }
    @Override
    public void setDetailFourText(String text) {
        detailFour.setText(text);
    }
    @Override
    public void setDetailFourHint(String hint) {
        detailFour.setHint(hint);
    }

    public void setProfilePic() {
        // Refresh imageview without cache after edit information
        Picasso.with(getContext())
                .load(userLocalStore.getLoggedInUser().getUserImageURL())
                .fit()
                .centerCrop()
                .into(profilePic);
    }
    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }
    @Override
    public void setViewVisible() {
        relativeFragProfile.setVisibility(View.VISIBLE);
    }
    @Override
    public void setDetailOneColor() {
        if (context != null) detailOne.setTextColor(ContextCompat.getColor(context, R.color.grayHint));
    }
    @Override
    public void setDetailTwoColor() {
        if (context != null) detailTwo.setTextColor(ContextCompat.getColor(context, R.color.grayHint));
    }
    @Override
    public void setDetailThreeColor() {
        if (context != null) detailThree.setTextColor(ContextCompat.getColor(context, R.color.grayHint));
    }
    @Override
    public void setDetailFourColor() {
        if (context != null) detailFour.setTextColor(ContextCompat.getColor(context, R.color.grayHint));
    }
    @Override
    public void isDetailOneAHint(boolean hint) {
        isDetailOneAHint = hint;
    }
    @Override
    public void isDetailTwoAHint(boolean hint) {
        isDetailTwoAHint = hint;
    }
    @Override
    public void isDetailThreeAHint(boolean hint) {
        isDetailThreeAHint = hint;
    }
    @Override
    public void isDetailFourAHint(boolean hint) {
        isDetailFourAHint = hint;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void displayError() {
        if (context != null) Helpers.displayErrorToast(context);
    }

    @Override
    public void hideImageTravel() {
        imgTravel.setVisibility(View.GONE);
    }

    @Override
    public void showImageTravel() {
        imgTravel.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideImageNotTravel() {
        imgNotTravel.setVisibility(View.GONE);
    }

    @Override
    public void showImageNotTravel() {
        imgNotTravel.setVisibility(View.VISIBLE);
    }

    @Override
    public void showTextTravel() {
        txtTravel.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideTextTravel() {
        txtTravel.setVisibility(View.GONE);
    }

    @Override
    public void showTextNotTravel() {
        txtNotTravel.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideTextNotTravel() {
        txtNotTravel.setVisibility(View.GONE);
    }

    @Override
    public void hideProgressDialog() {
        Helpers.hideProgressDialog(progressDialog);
    }
}
