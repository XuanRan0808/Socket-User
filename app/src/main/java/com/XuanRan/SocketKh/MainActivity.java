package com.XuanRan.SocketKh;

import android.app.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;


public class MainActivity extends Activity implements View.OnClickListener
{
    private TextView tv;
    private EditText et;
    private EditText IPet;
    private Handler myhandler;
    private Socket socket;
    private String str = "";
    boolean running = false;
    private Button btnSend;
    private Button btnStart;
    private Button btnStop;
    private StartThread st;
    private ReceiveThread rt;
    private TextView datatv;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.TV);
        et = (EditText) findViewById(R.id.et);
        IPet = (EditText) findViewById(R.id.IPet);
        datatv = findViewById(R.id.activitymainTextView1);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);

        setButtonOnStartState(true);//设置按键状态为可开始连接

        btnSend.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);

        myhandler = new MyHandler();//实例化Handler，用于进程间的通信

    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnStart:
                //按下开始连接按键即开始StartThread线程
                getMessage(IPet.getText().toString(),999);
//                st = new StartThread();
//                st.start();
                setButtonOnStartState(false);//设置按键状态为不可开始连接

                break;
            case R.id.btnSend:
                // 发送请求数据
                OutputStream os = null;
                try
                {

                    os = socket.getOutputStream();//得到socket的输出流
                    //输出EditText里面的数据，数据最后加上换行符才可以让服务器端的readline()停止阻塞
                    os.write((et.getText().toString() + "\n").getBytes("utf-8"));
                    et.setText("");//发送后输入框清0
//                    System.out.println(et.getText().toString()+"\n");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                try
                {
                    os.flush();
                }
                catch (IOException e)
                {
                    throw new RuntimeException("输出流刷新强制刷新失败" + e);
                }

                break;
            case R.id.btnStop:
                running = false;
                setButtonOnStartState(true);//设置按键状态为不可开始连接
                try
                {
                    socket.close();
                }
                catch (NullPointerException e)
                {
                    e.printStackTrace();
                    displayToast("未连接成功");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                break;
        }

    }
    private class StartThread extends Thread{
        @Override
        public void run() {
            try {

                socket = new Socket(IPet.getText().toString(),999);//连接服务端的IP
                
                getData();
                //启动接收数据的线程
//                rt = new ReceiveThread(socket);
//                rt.start();
//                running = true;
               // displayToast(socket.isConnected()+"");
                if(socket.isConnected()){//成功连接获取socket对象则发送成功消息
                    Message msg0 = myhandler.obtainMessage();
                    msg0.what=0;
                    myhandler.sendMessage(msg0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void getMessage(final String IP,final int Port) {
        new Thread() {

            @Override
            public void run() {
                try {
                    socket = new Socket(IP,Port);
                   // Scanner scanner=new Scanner(socket.getInputStream());
                    //scanner.useDelimiter("\n");
                    
                    boolean flag=true;
                    while(flag)
                    {
                        InputStream inputStream = socket.getInputStream();
                        DataInputStream input = new DataInputStream(inputStream);
                        byte[] b = new byte[10000];
                        int length = input.read(b);
                        final String Msg = new String(b, 0, length, "gb2312");

                        //_getMessage.sendMessage(new Message(msg));
                        runOnUiThread(new Runnable(){

                                @Override
                                public void run()
                                {
                                    datatv.append(Msg);
                                }
                                
                            
                        });
//                        socket.close();
//                        inputStream.close();
//                        input.close();
                    }
                    
                }catch(Exception ex)
                {
                    ex.printStackTrace();
                }

            }
        }.start();
    }
    
    
    
        public void getData()
        {
            try
            {
                // 创建读取数据的Reader,里面指定了需要的编码类型。
                BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                                                                             "UTF-8"));
                // 读取每一行的数据.注意大部分端口操作都需要交互数据。
                String str;
                
                while ((str = rd.readLine()) != null)
                {
                    
                    System.out.println(str);
                    tv.append(str);
                   // throw new RuntimeException("获得数据"+str);
                    
                }
                st.stop();
                socket.close();
                rd.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException("获取数据失败" + e);
         
            }
        }
        public void getMessage(Socket sock)
        {
            //提交对象
            //PrintWriter printWriter = new PrintWriter(sock.getOutputStream());//用于发送请求
            try
            {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                String responseData = bufferedReader .readLine();
                //标签存储状态
                boolean flag=true;
                while (flag)
                {
                    if (responseData != null)
                    {
                        displayToast("Socket得到数据:"+responseData);
//                        datatv.setText(bufferedReader.readLine().toString());
//                        
//                        tv.setText(responseData.toString());
                        flag = false;
                    }
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException("获取数据失败" + e);
            }


        }
    

    private class ReceiveThread extends Thread
    {
        private InputStream is;
        //建立构造函数来获取socket对象的输入流
        public ReceiveThread(Socket socket) throws IOException
        {
            is = socket.getInputStream();
        }
        @Override
        public void run()
        {
            while (running)
            {
                InputStreamReader isr = new InputStreamReader(is);
                final BufferedReader br = new BufferedReader(isr);
                try
                {
                    //读服务器端发来的数据，阻塞直到收到结束符\n或\r
                    System.out.println(str = br.readLine());
                    
                    throw new RuntimeException("数据已获得：" + br.readLine());
//                             
//                    runOnUiThread(new Runnable(){
//
//                            @Override
//                            public void run()
//                            {
//                                try
//                                {
//                                    datatv.setText(br.readLine());
//                                }
//                                catch (IOException e)
//                                {
//                                    throw new RuntimeException("Ui线程运行错误：" + e);
//                                }
//                            }
//                        });

                }
                catch (NullPointerException e)
                {
                    running = false;//防止服务器端关闭导致客户端读到空指针而导致程序崩溃
                    Message msg2 = myhandler.obtainMessage();
                    msg2.what = 2;
                    myhandler.sendMessage(msg2);//发送信息通知用户客户端已关闭
                    e.printStackTrace();
                    break;

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                //用Handler把读取到的信息发到主线程
                Message msg = myhandler.obtainMessage();


                msg.what = 1;
//                
                msg.obj = str;
                myhandler.sendMessage(msg);
                try
                {
                    sleep(400);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

            }
            Message msg2 = myhandler.obtainMessage();
            msg2.what = 2;
            myhandler.sendMessage(msg2);//发送信息通知用户客户端已关闭

        }
    }

    private void displayToast(String s)//Toast方法
    {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void setButtonOnStartState(boolean flag)
    {//设置按钮的状态
        btnSend.setEnabled(!flag);
        btnStop.setEnabled(!flag);
        btnStart.setEnabled(flag);
        IPet.setEnabled(flag);
    }


    class MyHandler extends Handler
    {//在主线程处理Handler传回来的message
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 1:
                    String str = (String) msg.obj;
                    System.out.println(msg.obj);
                    tv.setText(str);//把读到的内容更新到UI
                    break;
                case 0:
                    displayToast("连接成功");
                    break;
                case 2:
                    displayToast("服务器端已断开");
                    tv.setText(null);
                    setButtonOnStartState(true);//设置按键状态为可开始
                    break;

            }

        }
    }

}
