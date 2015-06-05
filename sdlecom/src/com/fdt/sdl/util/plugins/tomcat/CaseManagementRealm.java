package com.fdt.sdl.util.plugins.tomcat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

//import org.apache.catalina.realm.JDBCRealm;

/* Commented Temporarily **/

public class CaseManagementRealm { /*extends JDBCRealm {

	protected synchronized String getPassword(String username) {
		String encryptedPassword = null;
		encryptedPassword = super.getPassword(username);
		return AiCMSEncrypterDecrypter.decrypt(encryptedPassword);
	}

	protected synchronized PreparedStatement roles(Connection dbConnection,
			String username) throws SQLException {

		if (preparedRoles == null) {
			String SQL = "SELECT APPSECGROUPS.NAME "
					+ "FROM USERINFORMATION  "
					+ "INNER JOIN USERSECURITYRIGHTS ON USERSECURITYRIGHTS.USERID = USERINFORMATION.USER_ID "
					+ "INNER JOIN APPSECGROUPS ON APPSECGROUPS.ID = USERSECURITYRIGHTS.GROUPID "
					+ "where USERINFORMATION.LOGON_NAME = ? ";
			preparedRoles = dbConnection.prepareStatement(SQL);
		}

		preparedRoles.setString(1, username);
		return (preparedRoles);
	} */

}