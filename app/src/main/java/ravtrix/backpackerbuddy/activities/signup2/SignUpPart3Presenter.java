package ravtrix.backpackerbuddy.activities.signup2;

import android.app.Activity;

import com.google.gson.JsonObject;

import java.util.HashMap;

import ravtrix.backpackerbuddy.models.LoggedInUser;
import ravtrix.backpackerbuddy.models.UserLocalStore;

/**
 * Created by Ravinder on 9/28/16.
 */

class SignUpPart3Presenter implements ISignUpPart3Presenter {

    private ISignUpPart3View iSignUpPart3View;
    private SignUpPart3Interactor signUpPart3Interactor;
    private UserLocalStore userLocalStore;

    SignUpPart3Presenter(ISignUpPart3View iSignUpPart3View) {
        this.iSignUpPart3View = iSignUpPart3View;

        userLocalStore = new UserLocalStore((Activity) iSignUpPart3View);
        signUpPart3Interactor = new SignUpPart3Interactor();
    }


    @Override
    public void retrofitStoreUser(final HashMap<String, String> userInfo) {
        signUpPart3Interactor.signUserUpRetrofit(userInfo, new OnRetrofitSignUp3() {
            @Override
            public void onSuccess(JsonObject jsonObject) {

                iSignUpPart3View.hideProgressBar();
                if (userInfo.get("country").isEmpty()) { //empty because IOException thrown
                    // Update country info after the user signs up
                    iSignUpPart3View.updateCountry(userInfo.get("username"));
                }
                // Set new user local store
                long currentTime = System.currentTimeMillis();
                LoggedInUser user = new LoggedInUser();
                user.setUsername(userInfo.get("username"));
                user.setEmail(userInfo.get("email"));
                user.setUserID(jsonObject.get("id").getAsInt());
                user.setUserImageURL(jsonObject.get("userpic").getAsString());
                user.setLatitude(Double.valueOf(userInfo.get("latitude")));
                user.setLongitude(Double.valueOf(userInfo.get("longitude")));
                user.setTime(currentTime);
                user.setIsFacebook(0); // Not facebook user
                userLocalStore.storeUserData(user);
                iSignUpPart3View.startUserMainPage();
            }

            @Override
            public void onFailure() {
                iSignUpPart3View.hideProgressBar();
                iSignUpPart3View.showAlertDialogError();
            }
        });
    }

    @Override
    public void updateCountry(String username, String country) {
        signUpPart3Interactor.updateCountry(username, country, new OnRetrofitSignUp3() {
            @Override
            public void onSuccess(JsonObject jsonObject) {}
            @Override
            public void onFailure() {}
        });
    }
}
