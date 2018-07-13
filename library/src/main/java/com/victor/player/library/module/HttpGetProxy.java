package com.victor.player.library.module;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;

public class HttpGetProxy {
    final static private String TAG = "HttpGetProxy";
    final static private String LOCAL_IP_ADDRESS = "127.0.0.1";
    final static private int HTTP_PORT = 80;

    private int local_ip_port;
    private ServerSocket localServer = null;
    private Socket localSocket = null;
    private Socket remoteSocket = null;
    private String remoteHost;

    private InputStream in_remoteSocket;
    private OutputStream out_remoteSocket;
    private InputStream in_localSocket;
    private OutputStream out_localSocket;

    private SocketAddress address;
    private interface OnFinishListener {
        void onFinishListener();
    }

    /**
     * 初始化代理服务器
     * @param localport 代理服务器监听的端口
     */
    public HttpGetProxy(int localport) {
        local_ip_port=localport;
        try {
            localServer = new ServerSocket(localport, 1,
                    InetAddress.getByName(LOCAL_IP_ADDRESS));

            //启动代理服务器
            new Thread() {
                public void run() {
                    try {
                        startProxy();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 结束时，清除所有资源
     */
    private OnFinishListener finishListener = new OnFinishListener() {

        @Override
        public void onFinishListener() {
            System.out.println("..........release all..........");
            Log.e(TAG, "..........release all..........");
            try {
                in_localSocket.close();
                out_remoteSocket.close();

                in_remoteSocket.close();
                out_localSocket.close();

                localSocket.close();
                remoteSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };


    /**
     * 把网络URL转为本地URL，127.0.0.1替换网络域名
     * @param url 网络URL
     * @return 本地URL
     */
    public String getLocalURL(String url){
        String result = null;
        URI originalURI = URI.create(url);
        remoteHost = originalURI.getHost();
        if(originalURI.getPort()!=-1){//URL带Port
            address = new InetSocketAddress(remoteHost,
                    originalURI.getPort());//使用默认端口
            result = url.replace(remoteHost+":"+originalURI.getPort(),
                    LOCAL_IP_ADDRESS+":"+local_ip_port);
        } else{//URL不带Port
            address = new InetSocketAddress(remoteHost,HTTP_PORT);//使用80端口
            result=url.replace(remoteHost,LOCAL_IP_ADDRESS+":"+local_ip_port);
        }

        return result;

    }

    /**
     * 启动代理服务器
     * @throws IOException
     */
    public void startProxy() throws IOException {

        new Thread() {
            public void run() {
                int bytes_read;
                byte[] local_request = new byte[1024];
                byte[] remote_reply = new byte[1024];
                while (true) {
                    try {
                        //--------------------------------------
                        //监听MediaPlayer的请求，MediaPlayer->代理服务器
                        //--------------------------------------
                        localSocket = localServer.accept();

                        Log.e(TAG, "..........localSocket connected..........");
                        in_localSocket = localSocket.getInputStream();
                        out_localSocket = localSocket.getOutputStream();
                        Log.e(TAG, "..........init local Socket I/O..........");

                        String buffer = "";//保存MediaPlayer的HTTP请求
                        while ((bytes_read = in_localSocket.read(local_request)) != -1) {
                            String str = new String(local_request);
                            Log.e("localSocket---->", str);
                            buffer = buffer + str;
                            if (buffer.contains("GET")
                                    && buffer.contains("\r\n\r\n")) {
                                // ---把request中的本地ip改为远程ip---//
                                buffer = buffer.replace(LOCAL_IP_ADDRESS,remoteHost);
                                break;
                            }
                        }
                        Log.e(TAG, "..........local finish receive..........");

                        //--------------------------------------
                        //把MediaPlayer的请求发到网络服务器，代理服务器->网络服务器
                        //--------------------------------------
                        remoteSocket = new Socket();
                        remoteSocket.connect(address);
                        Log.e(TAG,"..........remote Server connected..........");
                        in_remoteSocket = remoteSocket.getInputStream();
                        out_remoteSocket = remoteSocket.getOutputStream();
                        out_remoteSocket.write(buffer.getBytes());//发送MediaPlayer的请求
                        out_remoteSocket.flush();

                        //------------------------------------------------------
                        //把网络服务器的反馈发到MediaPlayer，网络服务器->代理服务器->MediaPlayer
                        //------------------------------------------------------
                        Log.e(TAG,"..........remote start to receive..........");
                        while ((bytes_read = in_remoteSocket.read(remote_reply)) != -1) {
                            out_localSocket.write(remote_reply, 0, bytes_read);
                            out_localSocket.flush();
                        }
                        Log.e(TAG, "..........over..........");
                        finishListener.onFinishListener();//释放资源
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}
