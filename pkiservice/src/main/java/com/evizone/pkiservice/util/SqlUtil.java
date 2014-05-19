package com.evizone.pkiservice.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.inject.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@Singleton
public class SqlUtil {
	
	public boolean userExists(Connection conn, String cn) {
		try {
			String select = "select count(*) from principals where PrincipalID = ?";
	        PreparedStatement st = conn.prepareStatement(select);
	        st.setString(1, cn);
	        ResultSet rs = st.executeQuery();
	        int cnt = 0;
	 
			if(rs.next()) {
		        cnt = rs.getInt(1);
				System.out.println("Count of records: " + cnt);
		        if (cnt > 0) {
		        	return true;
		        } 
			}  
			 
			rs.close();  
			st.close();  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public String getPassword(Connection conn, String cn) {
        String pwd = null;
		try {
			String select = "SELECT CONCAT(`Password`,`PIN`) FROM principals WHERE PrincipalID = ?";
	        PreparedStatement st = conn.prepareStatement(select);
	        st.setString(1, cn);
	        ResultSet rs = st.executeQuery();
	 
			if(rs.next()) {
		        pwd = rs.getString(1);
				System.out.println("Password (" + cn + "): " + pwd);
			}  
			 
			rs.close();  
			st.close();  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return pwd;
	}	
	
	public int insertPrincipal(Connection conn, String cn, String password, int pin, String email, Integer phoneNo) {
		int res = 0;
		try {
			String insert = "insert into principals (PrincipalID, Password, PIN, Email, PhoneNo) values (?, ?, ?, ?, ?)";
	        PreparedStatement st = conn.prepareStatement(insert);
	        st.setString(1, cn);
	        st.setString(2, password);
	        st.setInt(3, pin);
	        st.setString(4, email);
	        st.setInt(5, phoneNo);
	        res = st.executeUpdate();
			 
			st.close();  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	public int insertRole(Connection conn, String cn) {
		int res = 0;
		try {
			String insert = "insert into roles (PrincipalID, Role, RoleGroup) values (?, ?, ?)";
	        PreparedStatement st = conn.prepareStatement(insert);
	        st.setString(1, cn);
	        st.setString(2, "user");
	        st.setString(3, "Roles");
	        res = st.executeUpdate();
			 
			st.close();  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	public Connection getJNDIConnection(String datasourceContext) {
		Connection result = null;
		try {
			Context initialContext = new InitialContext();
			DataSource datasource = (DataSource)initialContext.lookup(datasourceContext);
			if (datasource != null) {
				result = datasource.getConnection();
			} else {
				System.out.println("Failed to lookup datasource.");
			}
		} catch(NamingException ex) {
			System.out.println("Cannot get connection: " + ex);
		} catch(SQLException ex){
			System.out.println("Cannot get connection: " + ex);
		}
		return result;
	}	
	
}
