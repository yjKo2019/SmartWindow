package android.code.test;

import java.io.IOException;
import java.net.Socket;

import android.util.Log;

public class rcvthread implements Runnable {

	private logger logger;
	private final int sizeBuf = 50;
	private int flag;
	private Socket socket;
	private static String rcvData = "null";
	private byte[] rcvBuf = new byte[sizeBuf];
	private int rcvBufSize;

	public rcvthread(logger logger, Socket socket) {
		this.logger = logger;
		flag = 1;
		this.socket = socket;
	}

	public void setFlag(int setflag) {
		flag = setflag;
	}

	// 데이터를 수신받는 부분
	public void run() {
		while (flag == 1) {
			try {
				rcvBufSize = socket.getInputStream().read(rcvBuf);
				rcvData = new String(rcvBuf, 0, rcvBufSize, "UTF-8");

				Log.e("read test!!!", "read : " +rcvData);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.log("Exit loop");
	}

	// 다른 클래스에서 수신받은 데이터를 가져오기위한 함수
	static String get_data() {
		String s = rcvData;
		return s;
	}

	// 다른 클래스에서 수신받은 데이커를 지우기위한 함수
	static void delete_data() {
		rcvData = "null";
	}

}
