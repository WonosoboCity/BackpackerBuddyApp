package ravtrix.backpackerbuddy.activities.otheruserprofile;

/**
 * Created by Ravinder on 9/27/16.
 */

interface IOtherUserProfilePresenter {

    void fetchOtherUser(int userID);
    void hasBucket(int userID);
    void hasMap(int userID);

}
