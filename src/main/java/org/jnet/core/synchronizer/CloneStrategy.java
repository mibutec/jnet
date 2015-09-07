package org.jnet.core.synchronizer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jnet.core.helper.Unchecker;

public interface CloneStrategy {
	 <T> T clone(T object);
	
	public static final CloneStrategy serializer = new CloneStrategy() {
		
		@SuppressWarnings("unchecked")
		@Override
		public<T> T clone(T object) {
			return Unchecker.uncheck(() -> {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(object);
				oos.flush();
				oos.close();
				bos.close();
				byte[] byteData = bos.toByteArray();
				ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
				return (T) new ObjectInputStream(bais).readObject();
			});
		}
	};
}
