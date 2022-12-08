package sk.dzurikm.domestio.helpers;


public class Constants {
    public static final String SHARED_PREFERENCES_KEY = "domestio";

    public static final String LOGGED_IN = "loged_in";

    public static class Theme{
        public static final int LIGHT = 0;
        public static final int DARK = 1;
        public static final int SYSTEM = 2;
    }

    public static class Firebase{
        public static final String DOCUMENT_ROOMS = "Rooms";
        public static final String DOCUMENT_TASKS = "Tasks";
        public static final String DOCUMENT_USERS = "Users";

        public static class Room{
            public static final String FIELD_COLOR = "color";
            public static final String FIELD_DESCRIPTION = "description";
            public static final String FIELD_TASK_IDS = "task_ids";
            public static final String FIELD_TITLE = "title";
            public static final String FIELD_USER_IDS = "user_ids";
            public static final String FIELD_ADMIN_ID = "room_admin_id";
            public static final String FIELD_CREATED_AT = "created_at";
            public static final String FIELD_MODIFIED_AT = "modified_at";

        }

        public static class Task{
            public static final String FIELD_DONE = "done";
            public static final String FIELD_DESCRIPTION = "description";
            public static final String FIELD_HEADING = "heading";
            public static final String FIELD_TITLE = "title";
            public static final String FIELD_USER_IDS = "user_ids";
            public static final String FIELD_AUTHOR_ID = "author_user_id";
            public static final String FIELD_RECEIVER_ID = "receiving_user_id";
            public static final String FIELD_ROOM_ID = "room_id";
            public static final String FIELD_TIME = "time";
            public static final String FIELD_CREATED_AT = "created_at";
            public static final String FIELD_MODIFIED_AT = "modified_at";
        }

        public static class User{
            public static final String FIELD_ID = "id";
            public static final String FIELD_NAME = "name";
            public static final String FIELD_EMAIL = "email";
            public static final String FIELD_CREATED_AT = "created_at";
            public static final String FIELD_MODIFIED_AT = "modified_at";
        }

        public static class Bundle{
            public static final String ROOMS = "rooms";
            public static final String USERS = "users";
            public static final String TASKS = "tasks";
        }


        public static final int DATA_FOR_USER = 0;
        public static final int DATA_FOR_ROOM = 1;
        public static final int DATA_FOR_TASK = 2;
    }

    public static class Url{
        public static final String FACEBOOK = "https://www.facebook.com/profile.php?id=100009386056819";
        public static final String LINKED_ID = "https://www.linkedin.com/in/michal-dzurik/";
        public static final String GIT_HUB = "https://github.com/Michal-Dzurik";
        public static final String BUY_ME_A_COFFEE = "https://buymeacoffee.com/dzurikm";

    }

    public static class Result{
        public static final int ROOM_ACTIVITY = 0;
        public static final int PROFILE_ACTIVITY = 1;
    }

    public static class Validation{

        public static class Room{
            public static final String TITLE = Firebase.Room.FIELD_TITLE;
            public static final String DESCRIPTION = Firebase.Room.FIELD_DESCRIPTION;

        }

        public static class Task{
            public static final String HEADING = Firebase.Task.FIELD_HEADING;
            public static final String DESCRIPTION = Firebase.Task.FIELD_DESCRIPTION;
            public static final String TIME = "time";
            public static final String USER_ID = "userID";
            public static final String ROOM_ID = "roomID";
        }

        public static final String NAME = "name";

        public static final String EMAIL = "email";

        public static final String PASSWORD = "pass";
        public static final int PASS_MIN_LENGTH = 6;
        public static final String PASSWORD_REPEAT_DELIMITER = " ";

        public static final String PASSWORD_REPEAT = "pass_r";
    }

    public static class TextPrint{
        public static class Task{
            public static final int ROOM_NAME_MAX_CHAR = 6;
            public static final int USER_NAME_MAX_CHAR = 6;
            public static final int HEADING_NAME_MAX_CHAR = 18;
        }
    }

    public static class Settings{
        public static final String COLLAPSED_STATE = "collapsed_state";

        public enum CollapsingState{
            COLLAPSED,EXPANDED;

            public static String[] getNames() {
                CollapsingState[] states = values();
                String[] names = new String[states.length];

                for (int i = 0; i < states.length; i++) {
                    names[i] = Helpers.allLowercaseButFirstUppercase(states[i].name());
                }

                return names;
            }
        }

    }

    public static class NotificationChannels{
        public static String GENERAL = "General";
        public static String ROOM_UPDATES = "Room updates";
        public static String TASK_UPDATES = "Tasks updates";
        public static String NEW_JOINED_ROOM = "Joined room updates";
        public static String NEW_TASKS = "New tasks";

    }

}
