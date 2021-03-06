package ravtrix.backpackerbuddy.activities.otheruserprofile;

import com.google.gson.JsonObject;

/**
 * Created by Ravinder on 9/27/16.
 */

class OtherUserProfilePresenter implements IOtherUserProfilePresenter {

    private OtherUserProfileInteractor otherUserProfileInteractor;
    private IOtherUserProfileView view;

    OtherUserProfilePresenter(IOtherUserProfileView iOtherUserProfileView) {
        this.view = iOtherUserProfileView;
        otherUserProfileInteractor = new OtherUserProfileInteractor();
    }

    @Override
    public void fetchOtherUser(final int userID) {
        otherUserProfileInteractor.fetchOtherUserInfoRetrofit(userID, new OnRFOtherUserProfileListener() {
            @Override
            public void onSuccess(JsonObject otherUserJSON) {

                view.setUsername(otherUserJSON.get("username").getAsString());
                view.setUserDetailOne(otherUserJSON.get("detailOne").getAsString());
                view.setUserDetailTwo(otherUserJSON.get("detailTwo").getAsString());
                view.setUserDetailThree(otherUserJSON.get("detailThree").getAsString());
                view.setUserDetailFour(otherUserJSON.get("detailFour").getAsString());
                view.setUserLocation(otherUserJSON.get("latitude").getAsString(),
                       otherUserJSON.get("longitude").getAsString(), otherUserJSON.get("country").getAsString());
                view.loadProfileImage(otherUserJSON.get("userpic").getAsString());

                if (otherUserJSON.get("traveling").getAsInt() == 0) {
                    // User is not traveling
                    view.showImgNotTravel();
                    view.showTxtNotTravel();
                } else {
                    // User is traveling
                    view.showImgTravel();
                    view.showTxtTravel();
                }

                // Show bucket floating button if the other user set it to public
                if(otherUserJSON.get("bucketStatus").getAsInt() == 1) {
                    view.showFloatingButtonBucket();
                }
                view.showFloatingButtonMessage();
            }

            @Override
            public void onFailure() {
                view.displayErrorToast();
            }
        });
    }

    @Override
    public void hasBucket(int userID) {

        otherUserProfileInteractor.checkHasBucket(userID, new OnRFOtherUserProfileListener() {
            @Override
            public void onSuccess(JsonObject otherUserJSON) {

                // User has at least one item on their bucket list
                if (otherUserJSON.get("success").getAsInt() == 1) {
                    view.startBucketActivity();
                } else {
                    view.showNoBucket();
                }
            }

            @Override
            public void onFailure() {}
        });
    }

    @Override
    public void hasMap(int userID) {

        otherUserProfileInteractor.checkHasMap(userID, new OnRFOtherUserProfileListener() {
            @Override
            public void onSuccess(JsonObject otherUserJSON) {

                // user has at least one point of their map
                if (otherUserJSON.get("success").getAsInt() == 1) {
                    view.startMapActivity();
                } else {
                    view.showNoMap();
                }
            }

            @Override
            public void onFailure() {}
        });
    }
}
