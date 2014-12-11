package org.jnet.core.connection.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.jnet.core.connection.Connection;

public interface Message {
	void write(DataOutputStream stream) throws Exception ;
	void read(DataInputStream stream) throws Exception ;
	void write(OutputStream stream) throws Exception ;
	void read(InputStream stream) throws Exception ;
	Connection sender();
	void setSender(Connection connection);
}
