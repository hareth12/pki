package com.evizone.pkiservice.util;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PrivateKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.security.cert.X509Certificate;

import javax.sql.DataSource;

import org.jscep.transaction.TransactionId;

@Singleton
public class SqlUtil {
	
	public boolean userExists(Connection conn, String cn) {
		try {
			String select = "select count(*) from Principals where PrincipalID = ?";
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
			String select = "SELECT CONCAT(`Password`,`PIN`) FROM Principals WHERE PrincipalID = ?";
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
	
	public TransactionId getTransactionId(Connection conn, String cn) {
		byte[] bytes = null;
		TransactionId tid = null;
		try {
			String select = "SELECT TransactionID FROM Principals WHERE PrincipalID = ?";
	        PreparedStatement st = conn.prepareStatement(select);
	        st.setString(1, cn);
	        ResultSet rs = st.executeQuery();
	 
			if(rs.next()) {
				bytes = rs.getBytes(1);
				tid = (TransactionId)deserialize(bytes);
				System.out.println("TransactionID (" + cn + "): " + tid);
			}  
			 
			rs.close();  
			st.close();  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return tid;
	}
	
	public PrivateKey getPrivateKey(Connection conn, String cn) {
		byte[] bytes = null;
		PrivateKey pkey = null;
		try {
			String select = "SELECT PrivateKey FROM Principals WHERE PrincipalID = ?";
	        PreparedStatement st = conn.prepareStatement(select);
	        st.setString(1, cn);
	        ResultSet rs = st.executeQuery();
	 
			if(rs.next()) {
				bytes = rs.getBytes(1);
				pkey = (PrivateKey)deserialize(bytes);
				System.out.println("PrivateKey (" + cn + "): " + pkey);
			}  
			 
			rs.close();  
			st.close();  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return pkey;
	}			

	public X509Certificate getCert(Connection conn, String cn) {
		byte[] bytes = null;
		X509Certificate cert = null;
		try {
			String select = "SELECT Cert FROM Principals WHERE PrincipalID = ?";
	        PreparedStatement st = conn.prepareStatement(select);
	        st.setString(1, cn);
	        ResultSet rs = st.executeQuery();
	 
			if(rs.next()) {
				bytes = rs.getBytes(1);
				cert = (X509Certificate)deserialize(bytes);
				System.out.println("X509Certificate (" + cn + "): " + cert);
			}  
			 
			rs.close();  
			st.close();  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return cert;
	}			
	
	public int insertPrincipal(Connection conn, String cn, String password, int pin, String email, Integer phoneNo) {
		int res = 0;
		try {
			String insert = "insert into Principals (PrincipalID, Password, PIN, Email, PhoneNo) values (?, ?, ?, ?, ?)";
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
			String insert = "insert into Roles (PrincipalID, Role, RoleGroup) values (?, ?, ?)";
	        PreparedStatement st = conn.prepareStatement(insert);
	        st.setString(1, cn);
	        st.setString(2, "manager");
	        st.setString(3, "Roles");
	        res = st.executeUpdate();
			 
			st.close();  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	public byte[] serialize(Object obj) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {			
			ObjectOutputStream oos = new ObjectOutputStream(bos);			
			oos.writeObject(obj);
			oos.flush();
			oos.close();
			bos.close();		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bos.toByteArray();
	}
	
	public Object deserialize(byte[] obj) {
        Object out = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(obj);
	        ObjectInputStream ins = new ObjectInputStream(bais);
	        out = ins.readObject();
	        ins.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }		
        return out;
	}
	
	public int updatePrincipal(Connection conn, 
			String cn, 
			TransactionId transactionId,
			X509Certificate cert,
			PrivateKey privateKey) {
		int res = 0;
		try {
			String update = "update Principals set TransactionID = ?, Cert = ?, PrivateKey = ? where PrincipalID = ?";
	        PreparedStatement st = conn.prepareStatement(update);
	        st.setObject(1, serialize(transactionId));
	        st.setObject(2, serialize(cert));
	        st.setObject(3, serialize(privateKey));
	        st.setString(4, cn);
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
