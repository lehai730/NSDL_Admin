package info.androidhive.admin.app;

public class AppConfig {
	// Server user login url
	public static String URL_LOGIN = "http://192.168.1.120/admin_login_api/login.php";

	// Server user register url
	public static String URL_REGISTER = "http://192.168.1.120/admin_login_api/register.php";

	// Server user unlock url
	public static String URL_UNLOCK = "http://192.168.1.120/admin_login_api/unlock.php";

	// Server to get DoorUsers
	public static String URL_DOORUSER = "http://192.168.1.120/admin_login_api/getdoorUsers.php";

	// Server to get Logs
	public static String URL_LOGS = "http://192.168.1.120/admin_login_api/getLogs.php";

	// Server to clear Logs
	public static String URL_CLEAR_LOGS = "http://192.168.1.120/admin_login_api/clearLogs.php";

	// Server to delete Users
	public static String URL_DELETE_USERS = "http://192.168.1.120/admin_login_api/deleteUsers.php";

	// Server to update Users
	public static String URL_UPDATE_USERS = "http://192.168.1.120/admin_login_api/updateUsers.php";
}