package ravtrix.backpackerbuddy.activities.login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.squareup.leakcanary.LeakCanary;

import butterknife.BindView;
import butterknife.ButterKnife;
import ravtrix.backpackerbuddy.R;
import ravtrix.backpackerbuddy.activities.UserMainPage;
import ravtrix.backpackerbuddy.activities.WelcomeActivity;
import ravtrix.backpackerbuddy.baseActivitiesAndFragments.OptionMenuSendBaseActivity;
import ravtrix.backpackerbuddy.helpers.Helpers;
import ravtrix.backpackerbuddy.models.LoggedInUser;
import ravtrix.backpackerbuddy.models.UserLocalStore;
import ravtrix.backpackerbuddy.retrofit.retrofitrequests.retrofituserinforequests.RetrofitUserInfo;

/**
 * Created by Ravinder on 3/29/16.
 */
public class LogInActivity extends OptionMenuSendBaseActivity implements ILogInView {

    @BindView(R.id.etLoggedInUsername) protected EditText etLoggedInUsername;
    @BindView(R.id.etLoggedInPassword) protected EditText etLoggedInPassword;
    private UserLocalStore userLocalStore;
    private RetrofitUserInfo retrofitUser;
    private LogInPresenter logInPresenter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LeakCanary.install(getApplication());
        setContentView(R.layout.activity_login);
        setToolbar();
        ButterKnife.bind(this);
        logInPresenter = new LogInPresenter(this);
        userLocalStore = new UserLocalStore(this);
        retrofitUser = new RetrofitUserInfo();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.submitSend:
                System.out.println("LOGGING IN");
                logUserIn();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Log user in if right credentials given by the user
     * Animate bouncing effect if the field is empty
     */
    private void logUserIn() {
        String username = etLoggedInUsername.getText().toString();
        String password = etLoggedInPassword.getText().toString();

        if (username.isEmpty() && password.isEmpty()) {
            // Animation bounce if username AND password fields entered are empty
            YoYo.with(Techniques.Bounce).duration(500).playOn(findViewById(R.id.etLoggedInUsername));
            YoYo.with(Techniques.Bounce).duration(500).playOn(findViewById(R.id.etLoggedInPassword));

        } else if (username.isEmpty()) {
            // Animation bounce if username field entered is empty
            YoYo.with(Techniques.Bounce).duration(500).playOn(findViewById(R.id.etLoggedInUsername));
        } else if (password.isEmpty()) {
            // Animation bounce if password field entered is empty
            YoYo.with(Techniques.Bounce).duration(500).playOn(findViewById(R.id.etLoggedInPassword));
        } else {

            System.out.println("USERNAME: " + username) ;
            System.out.println("PASSWORD: " + password) ;

            logInPresenter.logUserIn(username, password);
        }
    }

    private void setToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarLogIn);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    // Error if the user info is incorrect
    @Override
    public void showNoUserErrorMessage(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Incorrect user details");
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.show();
    }

    @Override
    public void showProgressDialog() {
        this.progressDialog = Helpers.showProgressDialog(this, "Logging In...");
    }

    @Override
    public void hideProgressDialog() {
        Helpers.hideProgressDialog(progressDialog);
    }

    @Override
    public void saveNewUser(LoggedInUser loggedInUser) {
        userLocalStore.clearUserData();
        userLocalStore.storeUserData(loggedInUser);
    }

    @Override
    public void userCanLogIn() {
        startActivity(new Intent(LogInActivity.this, UserMainPage.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logInPresenter.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, WelcomeActivity.class));
    }
}
