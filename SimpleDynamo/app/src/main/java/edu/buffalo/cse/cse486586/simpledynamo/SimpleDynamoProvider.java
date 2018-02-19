package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;


public class SimpleDynamoProvider extends ContentProvider {

    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";

    static final int SERVER_PORT = 10000;
    static final String TAG = SimpleDynamoProvider.class.getSimpleName();
    SharedPreferences sharedPreferences;

    HashMap<String,String> recoveryMap = new HashMap<String, String>();
    private String portStr = "";
    private String myPort = "";

    private String remotePort[];
//    private String emulatorId[] = {"5554", "5556", "5558", "5560", "5562"};
    private static TreeMap<String,String> treeMap = new TreeMap<String,String>();

    private static ArrayList<String> arrayList = new ArrayList<String>();
    private static String msgId;
    String queryString = "";
    String [] querySelection;
//    String avdId;
//    String succ1;
//    String succ2;
//    ArrayList array = new ArrayList();
//    static ArrayList insertPorts = new ArrayList();

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

        Log.i(TAG, "delete delete delete delete");

        SharedPreferences sharedPrefs = sharedPreferences;
        SharedPreferences.Editor editor = sharedPrefs.edit();
        mutableClass deleteResult = new mutableClass();
        if(selection.equals("@")) {
            editor.clear();
            editor.commit();
        } else if(selection.equals("*")) {
            String ports[] = {"11124", "11112", "11108", "11116", "11120"};
            for(int i = 0; i<ports.length; i++) {
                new ClientTask(deleteResult).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "clear everything"); //, String.valueOf(Integer.parseInt(ports[i])), selection + "," + "@");
            }
        } else{
            try {
                msgId = genHash(selection);
                Log.i("hashed value delete", "msg id is:" + selection);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            ArrayList<String> stringArraylist = (ArrayList<String>) arrayList.clone();
            stringArraylist.add(msgId);
            Collections.sort(stringArraylist);
            Log.i("array contents delete", stringArraylist.toString());

            int posMsgId = stringArraylist.indexOf(msgId);

            String targetAvdPort, succPorts1,succPorts2;

            targetAvdPort = stringArraylist.get((posMsgId+1)%stringArraylist.size());
            targetAvdPort = treeMap.get(targetAvdPort);
            succPorts1 = stringArraylist.get((posMsgId+2)%stringArraylist.size());
            succPorts1 = treeMap.get(succPorts1);
            succPorts2 = stringArraylist.get((posMsgId+3)%stringArraylist.size());
            succPorts2 = treeMap.get(succPorts2);

            Log.e("Insert","3 ports are - " + targetAvdPort + ", " + succPorts1 + ", " + succPorts2);

            if (targetAvdPort.equals(myPort)) {
                Log.i(TAG,"inside delete cond1");
                editor.clear();
                editor.commit();
                Log.i("editor contents", editor.toString());
            } else {
                Log.i("spawn CT delete cond1", targetAvdPort);
                new ClientTask(deleteResult).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "delete", targetAvdPort, selection + "," + "@");
            }
            if (succPorts1.equals(myPort)) {
                Log.i(TAG,"inside delete cond2");
                editor.clear();
                editor.commit();
                Log.i("editor contents", editor.toString());
            } else {
                Log.i("spawn CT delete cond2", succPorts1);
                new ClientTask(deleteResult).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "delete", succPorts1, selection + "," + "@");
             }
            if (succPorts2.equals(myPort)) {
                Log.i(TAG,"inside delete cond3");
                editor.clear();
                editor.commit();
                Log.i("editor contents", editor.toString());
            } else {
                Log.i("spawn CT delete cond3", succPorts2);
                new ClientTask(deleteResult).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "delete", succPorts2, selection + "," + "@");
            }
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub

        SharedPreferences.Editor editor = sharedPreferences.edit();
        mutableClass insertResult = new mutableClass();
        try {
            msgId = genHash(values.getAsString(KEY_FIELD));
            Log.i("msg insert", "key to insert is :" + values.getAsString(KEY_FIELD));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        String message = values.getAsString(KEY_FIELD) + "," + values.getAsString(VALUE_FIELD);
        String targetAvdPort="";
        ArrayList<String> arrayList1 = (ArrayList<String>) arrayList.clone();
        arrayList1.add(msgId);
        Collections.sort(arrayList1);
//        Log.i("check array insert", arrayList1.toString());

        int posMsgId=arrayList1.indexOf(msgId);

        String succPorts1;
        String succPorts2;

        targetAvdPort = arrayList1.get((posMsgId+1)%arrayList1.size());
        targetAvdPort = treeMap.get(targetAvdPort);
        succPorts1 = arrayList1.get((posMsgId+2)%arrayList1.size());
        succPorts1 = treeMap.get(succPorts1);
        succPorts2 = arrayList1.get((posMsgId+3)%arrayList1.size());
        succPorts2 = treeMap.get(succPorts2);

        Log.e("Insert","3 ports are - " + targetAvdPort + ", " + succPorts1 + ", " + succPorts2);

        if (targetAvdPort.equals(myPort)){
            Log.i(TAG,"targetAvdPort is mine");
            editor.putString(values.getAsString(KEY_FIELD), values.getAsString(VALUE_FIELD));
            editor.commit();
            Log.i("editor contents", String.valueOf(editor));
        }else{
            Log.i("spawn CT insert cond1", targetAvdPort.toString());
            new ClientTask(insertResult).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "insert", targetAvdPort, message);
        }
        if (succPorts1.equals(myPort)){
            Log.i(TAG,"inside insert condition 2");
            editor.putString(values.getAsString(KEY_FIELD), values.getAsString(VALUE_FIELD));
            editor.commit();
            Log.i("editor contents", String.valueOf(editor));
        }else {
            Log.i("spawn CT insert cond2", succPorts1.toString());
            new ClientTask(insertResult).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "insert", succPorts1, message);
        }
        if (succPorts2.equals(myPort)){
            Log.i(TAG,"inside insert condition 3");
            editor.putString(values.getAsString(KEY_FIELD), values.getAsString(VALUE_FIELD));
            editor.commit();
            Log.i("editor contents", String.valueOf(editor));
        }else {
            Log.i("spawn CT insert cond3", succPorts2.toString());
            new ClientTask(insertResult).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "insert", succPorts2, message);
        }
//        for(int i = 0; i<arrayList1.size(); i++) {
//            if(arrayList1.get(i).compareTo(msgId) >=0) {
//                arrayList1.add(msgId);
//                avdId = String.valueOf(treeMap.get(arrayList1.get((i) % 5)));
//                editor.putString(values.getAsString(KEY_FIELD), values.getAsString(VALUE_FIELD));
//                editor.commit();
//
//                succ1 = String.valueOf(treeMap.get(arrayList1.get((i + 1) % 5)));
//                succ2 = String.valueOf(treeMap.get(arrayList1.get((i + 2) % 5)));
//
//                insertPorts.add(avdId);
//                insertPorts.add(succ1);
//                insertPorts.add(succ2);
//
//                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "insert into nodes" , String.valueOf(Integer.parseInt(succ1)) ,  String.valueOf(Integer.parseInt(succ2)) , message);
//            }
//            else {
//                avdId = String.valueOf(treeMap.get(arrayList1.get((i) % 5)));
//                succ1 = String.valueOf(treeMap.get(arrayList1.get((i + 1) % 5)));
//                succ2 = String.valueOf(treeMap.get(arrayList1.get((i + 2) % 5)));
//
//                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "directly" , String.valueOf(Integer.parseInt(avdId)), String.valueOf(Integer.parseInt(succ1)) ,  String.valueOf(Integer.parseInt(succ2)) , message);
//            }
//        }

//        for(int i =0; i<arrayList.size(); i++) {
//            if (arrayList.get(i).compareTo(msgId) >= 0) {
//                avdId = String.valueOf(treeMap.get(arrayList.get((i) % 5)));
//                succ1 = String.valueOf(treeMap.get(arrayList.get((i + 1) % 5)));
//                succ2 = String.valueOf(treeMap.get(arrayList.get((i + 2) % 5)));
//                Log.i("values", "avdId:" + avdId + " " + "succ1" + succ1 + " " + "succ2" + succ2);
//
//                insertPorts.add(avdId);
//                insertPorts.add(succ1);
//                insertPorts.add(succ2);
//
//                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "insert into nodes", String.valueOf(Integer.parseInt(avdId)), String.valueOf(Integer.parseInt(succ1)), String.valueOf(Integer.parseInt(succ2)), message);
//            }
//        }
//
//                avdId = String.valueOf(treeMap.get(arrayList.get(0)));
//                succ1 = String.valueOf(treeMap.get(arrayList.get(1)));
//                succ2 = String.valueOf(treeMap.get(arrayList.get(2)));
//                Log.i("values", "avdId:" + avdId +" " + "succ1" +succ1 + " " + "succ2" + succ2);
//
//                insertPorts.add(avdId);
//                insertPorts.add(succ1);
//                insertPorts.add(succ2);
//
//                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "insert into nodes", String.valueOf(Integer.parseInt(avdId)), String.valueOf(Integer.parseInt(succ1)), String.valueOf(Integer.parseInt(succ2)), message);

//        if(msgId.compareTo(String.valueOf(arrayList))> 0) {
//            editor.putString(values.getAsString(KEY_FIELD), values.getAsString(VALUE_FIELD));
//            editor.commit();
//            for(int i = 0; i<arrayList.size(); i++) {
//                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "insert into succ1", myPort);
//                insertPorts.add(myPort);
//            }
//        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.v("insert", values.toString());
        return uri;
        //return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub

        TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        sharedPreferences  = getContext().getSharedPreferences(PREFS_NAME, 4);

        try {
            remotePort = new String [] {"11124", "11112", "11108", "11116", "11120"};
            String emulatorId[] = new String[] {genHash(String.valueOf(5562)), genHash(String.valueOf(5556)), genHash(String.valueOf(5554)), genHash(String.valueOf(5558)), genHash(String.valueOf(5560))};

            for(int i = 0; i<emulatorId.length; i++) {
                arrayList.add(emulatorId[i]);
                Log.i(TAG, "arrayList contents:" + arrayList);
            }
//            for(int i = 0; i<emulatorId.length; i++) {
//                for(int j =0; j<remotePort.length; j++) {
//                    treeMap.put(emulatorId[i], remotePort[j]);
//                    Log.i(TAG, "treemap contents:" + treeMap);
//                }
//            }
//            arrayList.add(emulatorId[0]);
//            arrayList.add(emulatorId[1]);
//            arrayList.add(emulatorId[2]);
//            arrayList.add(emulatorId[3]);
//            arrayList.add(emulatorId[4]);
//            Log.i(TAG, "arraylist content:" + arrayList);

            treeMap.put(emulatorId[0], remotePort[0]);
            treeMap.put(emulatorId[1], remotePort[1]);
            treeMap.put(emulatorId[2], remotePort[2]);
            treeMap.put(emulatorId[3], remotePort[3]);
            treeMap.put(emulatorId[4], remotePort[4]);
            Log.i(TAG, "treeMap contents:" + treeMap);

            //new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "new node join", myPort);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
        }

        new ClientTask(new mutableClass()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"recovery");

        return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub


        Log.i(TAG, "Checking selection" + selection);
        mutableClass resultOfQuery = new mutableClass();
        selection = selection.trim();
        //String message = String.valueOf(query(mUri, null, selection, null, null));
        if (selection.contains(",")) {
            querySelection = selection.split(",");
            selection = querySelection[0];
            Log.i("query selection:", querySelection[0] + "," + querySelection[1]);
        }
        Log.i(TAG, String.valueOf(arrayList.size()));

            if (selection.equals("@")) {
                Log.i(TAG, "Printing keys top - @ query");
                MatrixCursor matrixCursor = new MatrixCursor(new String[]{KEY_FIELD, VALUE_FIELD});
                Map<String, ?> allEntries = sharedPreferences.getAll();

                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    Log.i("@ query Map entries", entry.getKey() + ": " + entry.getValue().toString());
                    matrixCursor.addRow(new Object[]{entry.getKey(), entry.getValue().toString()});
                }
                return matrixCursor;
            }
            else if (selection.equals("*")) {
                HashMap<String, String> hashMap = new HashMap<String, String>();

                MatrixCursor matrixCursor = new MatrixCursor(new String[]{KEY_FIELD, VALUE_FIELD});
                Map<String, ?> allEntries = sharedPreferences.getAll();

                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    Log.i("* query Map entries", entry.getKey() + ": " + entry.getValue().toString());
                    //hashMap.putAll((Map<? extends String, ? extends String>) allEntries);
                    hashMap.put(entry.getKey(), entry.getValue().toString());
                    //matrixCursor.addRow(new Object[]{entry.getKey(), entry.getValue().toString()});
                }
                Log.i(TAG, "spawning new client task * query");


                for(int i = 0; i < 5; i++) {
                    if(remotePort[i].equals(myPort))
                        continue;

                    new ClientTask(resultOfQuery).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "star", remotePort[i]);

                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        Log.i(TAG,"thread slept");
                        e.printStackTrace();
                    }

                    String receivedQuery = resultOfQuery.queryResult;

                    if(receivedQuery.equals("defaultValue"))
                        continue;

                    receivedQuery = receivedQuery.substring(0, receivedQuery.length()-1);
                    String [] rcv = receivedQuery.split("-");
                    for(int j = 0; j<rcv.length;j++) {
                        Log.e("StarQuery","keyVal-" + rcv[j]);
                        String [] str = rcv[j].split(",");
                        hashMap.put(str[0], str[1]);
                    }
                }
                for(Map.Entry<String, String> entry : hashMap.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    matrixCursor.addRow(new Object[]{key, value});
                    Log.i("check cursor", matrixCursor.toString());
                }

//                String receivedQuery = queryString;
//                receivedQuery = receivedQuery.substring(0, receivedQuery.length()-1);
//                Log.i("recQuery being checked", receivedQuery);
//                queryString = "";
//                String[] str = receivedQuery.split("-");
//                Log.i("str being checked", str.toString());
//                for(int j = 0; j<str.length; j++)
//                {
//                    matrixCursor.addRow(new Object[]{str[j].split(",")[0],str[j].split(",")[1]});
//                    Log.i("check cursor", matrixCursor.toString());
//                }
                return matrixCursor;
            }
        else {
                try {
                    msgId = genHash(selection);
                    Log.i("msg", "query selection is:" + selection);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                //String message = values.getAsString(KEY_FIELD) + "," + values.getAsString(VALUE_FIELD);
                String targetAvdPort = "";
                ArrayList<String> arrayList1 = (ArrayList<String>) arrayList.clone();
                arrayList1.add(msgId);
                Collections.sort(arrayList1);
                Log.i("sort sort sort", arrayList1.toString());

                int posMsgId = arrayList1.indexOf(msgId);
                Log.i("check position query", String.valueOf(posMsgId));

                String succPorts1;
                String succPorts2;

                targetAvdPort = arrayList1.get((posMsgId+1)%arrayList1.size());
                targetAvdPort = treeMap.get(targetAvdPort);
                succPorts1 = arrayList1.get((posMsgId+2)%arrayList1.size());
                succPorts1 = treeMap.get(succPorts1);
                succPorts2 = arrayList1.get((posMsgId+3)%arrayList1.size());
                succPorts2 = treeMap.get(succPorts2);

                if (targetAvdPort.equals(myPort) || succPorts1.equals(myPort) || succPorts2.equals(myPort)) {
                    MatrixCursor matrixCursor = new MatrixCursor(new String[]{KEY_FIELD, VALUE_FIELD});
                    String value = sharedPreferences.getString(selection,"");
                    matrixCursor.addRow(new Object[]{selection, value});
                    return matrixCursor;
                }
                else
                {
                    Log.i("spawn CT targetport que", targetAvdPort);
                    String [] portToAsk = new String [] {targetAvdPort,succPorts1,succPorts2};

                    for(int j = 0;j<portToAsk.length;j++) {

                        new ClientTask(resultOfQuery).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "queryPort", portToAsk[j], selection);
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        String queryResult = resultOfQuery.queryResult;
                        Log.e("Single Query","key: " + selection + " value: " + queryResult);

                        if(!queryResult.equals("defaultValue") && !queryResult.isEmpty())
                        {
                            MatrixCursor matrixCursor = new MatrixCursor(new String[]{KEY_FIELD, VALUE_FIELD});
                            matrixCursor.addRow(new Object[]{selection, queryResult});
                            return matrixCursor;
                        }
                    }
                }

            }
        return null;
    }

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

//    public ArrayList find_pred(String pred1, String pred2) {
//        int index = 0;
//
//        if(index == 0) {
//            pred1 = String.valueOf(treeMap.get(4));
//            pred2 = String.valueOf(treeMap.get(3));
//
//            Log.i("pred 1:" + pred1, "pred 2:" + pred2);
//        }
//        else if(index == 1) {
//            pred1 = String.valueOf(treeMap.get(0));
//            pred2 = String.valueOf(treeMap.get(4));
//
//            Log.i("pred 1:" + pred1, "pred 2:" + pred2);
//        }
//        else {
//            pred1 = String.valueOf(treeMap.get(index - 1));
//            pred2 = String.valueOf(treeMap.get(index - 2));
//
//            Log.i("pred 1:" + pred1, "pred 2:" + pred2);
//        }
//
//        ArrayList arrayList1 = new ArrayList();
//        arrayList1.add(pred1);
//        arrayList1.add(pred2);
//        return arrayList1;
//    }
//
//    public ArrayList find_succ(String succ1, String succ2) {
//        int index = 0;
//
//        if(index == 3) {
//            succ1 = String.valueOf(treeMap.get(4));
//            succ2 = String.valueOf(treeMap.get(0));
//
//            Log.i("succ 1:" +succ1, "succ 2:" + succ2);
//        }
//        else if(index == 4) {
//            succ1 = String.valueOf(treeMap.get(0));
//            succ2 = String.valueOf(treeMap.get(1));
//
//            Log.i("succ 1:" +succ1, "succ 2:" + succ2);
//        }
//        else {
//            succ1 = String.valueOf(treeMap.get(index + 1));
//            succ2 = String.valueOf(treeMap.get(index + 2));
//
//            Log.i("succ 1:" +succ1, "succ 2:" + succ2);
//        }
//
//        ArrayList arrayList1 = new ArrayList();
//        arrayList1.add(succ1);
//        arrayList1.add(succ2);
//        return arrayList1;
//    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @SuppressLint("CommitPrefEdits")
        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            int count = 0;
            BufferedReader bufferedReader;
            Log.i(TAG, "Server Started");

            try {
                while(count<10) {
                    Log.i(TAG, "entered while of server");
                    Socket socket = serverSocket.accept();
                    Log.i(TAG, "socket created");

                    PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String input = bufferedReader.readLine();
                    Log.i("input string contents", input);

//                    Log.i(TAG, input);
                    if (input != null) {
                        String[] arrayOfStrings = input.split(",");
                        Log.i("arrayofstrings content", arrayOfStrings[0]);

                        if(arrayOfStrings[0].equals("insert")) {
                            Log.i(TAG,"condition 1 of insert");
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(arrayOfStrings[1], arrayOfStrings[2]);
                            editor.commit();
                            Log.i("ServerTask","Insert here");

                            pw.println("ACK");
                            pw.flush();
                        }
                        else if(arrayOfStrings[0].equals("delete")) {
                            Log.i(TAG, "delete from target");
                            String selection = arrayOfStrings[2];
                            String strings[] = selection.split(",");
                            String str = strings[0];
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove(str);
                            editor.commit();
                        }
                        else if(arrayOfStrings[0].contains("star"))
                        {
                            Log.i(TAG, "star query part" + arrayOfStrings[0]);

                            Map<String, ?> allEntries = sharedPreferences.getAll();

                            String resultToSend = "";
                            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                                Log.i("@ query Map entries", entry.getKey() + ": " + entry.getValue().toString());
                                resultToSend = entry.getKey() + "," + entry.getValue().toString() + "-" + resultToSend;
                            }

                            pw.println(resultToSend);
                            pw.flush();


                        }
                        else if(arrayOfStrings[0].contains("queryPort")){
//                            Log.i(TAG, "inside server query queryPort");
                            Log.i("ServerQuery", "query requested : " +arrayOfStrings[1]);
//                            Log.i(TAG, "selection contents of queryPort:" + arrayOfStrings[2]);
                            String selection = arrayOfStrings[1];
                            String result = sharedPreferences.getString(selection,"");

                            Log.i("ServerQuery", "Result for key: " + selection + " is " + result);
                            pw.println(result);
                            pw.flush();
                        }
                        else if(arrayOfStrings[0].contains("recovery"))
                        {
                            String resultToSend = "";
                            for(Map.Entry<String,String> eachEntry:recoveryMap.entrySet())
                            {
                                resultToSend = eachEntry.getKey() + "," + eachEntry.getValue() + "-" + resultToSend;
                            }

                            recoveryMap.clear();
                            pw.println(resultToSend);
                            pw.flush();
                        }

                    }
                    bufferedReader.close();
                    socket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Couldn't connect with IO");
            }

            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "mood hua, log daala");
                e.printStackTrace();
            }
            return null;
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        private mutableClass returnValue;
        public ClientTask(mutableClass resultOfQuery) {
            returnValue = resultOfQuery;
        }

        @SuppressLint("CommitPrefEdits")
        @Override
        protected Void doInBackground(String... msgs) {
            try {

                String msgToSend = msgs[0];
                Log.i(TAG, "entering client task");
                Log.i("msgToSend contents", msgToSend);
                Log.i("msgs content", msgs[0]);

                if(msgToSend.contains("insert")) {
                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(msgs[1]));
                        Log.i("sending insert req to:" + msgs[1], "message is:" + msgs[2]);
                        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader buffReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        printWriter.println("insert" + "," + msgs[2]);
                        printWriter.flush();

                        String ack = buffReader.readLine();

                        if(ack==null)
                        {
                            recoveryMap.put(msgs[2].split(",")[0],msgs[2].split(",")[1]);
                        }
                        socket.close();
                }
                else if(msgToSend.contains("clear everything")) {
                    String ports[] = {"11124", "11112", "11108", "11116", "11120"};
                    for(int i = 0; i<ports.length; i++) {
                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(ports[i]));
                        Log.i("clear all messages", ports[i]);
                        //Log.i(" clear all messages:" + ports[i], "message is:" + msgs[2]);
                        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                        printWriter.println("clear everything" + "," + "@");
                        printWriter.flush();
                        socket.close();
                    }
                }
                else if(msgToSend.contains("delete")) {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(msgs[1]));
                    Log.i("direct delete req to:" + msgs[1], "message is:" + msgs[2]);
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    printWriter.println("delete" + "," + msgs[2]);
                    printWriter.flush();
                    socket.close();
                }
                else if(msgToSend.contains("star"))
                {
                    Log.i(TAG, "client part of star query");

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(msgs[1]));
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    printWriter.println("star"); // + "," + msgs[1]);
                    printWriter.flush();

                    returnValue.queryResult = bufferedReader.readLine();

                    if(returnValue.queryResult==null)
                        returnValue.queryResult="defaultValue";
                    Log.e("ClientStar","Allpairs- " + returnValue.queryResult);
                    socket.close();
                }
                else if(msgToSend.contains("queryPort"))
                {
                    String succPort = msgs[1];
                    Log.v("ClientQuery", "querying :" + succPort + " for key - " + msgs[2]);
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(succPort));
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader buffReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    printWriter.println("queryPort" + "," + msgs[2]);
                    printWriter.flush();

                    returnValue.queryResult = buffReader.readLine();

                    if(returnValue.queryResult==null)
                        returnValue.queryResult="defaultValue";

                    socket.close();
                }
                else if(msgToSend.contains("recovery"))
                {
                    for(int i = 0;i<remotePort.length;i++)
                    {
                        if(remotePort[i].equals(myPort))
                            continue;

                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(remotePort[i]));
                        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader buffReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                        printWriter.println("recovery");
                        printWriter.flush();

                        String recoveredValues = buffReader.readLine();
                        if(recoveredValues==null || recoveredValues.isEmpty())
                            continue;

                        recoveredValues = recoveredValues.substring(0, recoveredValues.length()-1);
                        String [] rcv = recoveredValues.split("-");
                        for(int j = 0; j<rcv.length;j++) {
                            Log.e("Recovery","keyVal-" + rcv[j]);
                            String [] str = rcv[j].split(",");
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(str[0],str[1]);
                            editor.commit();
                        }

                    }
                }


            } catch (UnknownHostException e) {
                Log.e(TAG, "host gaya tel lene");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "IO bhi usske peeche gaya");
                e.printStackTrace();
            }
            return null;
        }
    }


}