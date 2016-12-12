package com.example.marco.wifidirectconnection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Marco on 11/12/16.
 */

public class FileReceiveAsync extends AsyncTask<Void, Void, String>{

    //classe per ricevere dati in una porta del socket
    //TODO FAR IN MODO DI POTER SCEGLIERE LA PORTA IN BASE A QUELLO CHE VOGLIO FARE


    @Override
    protected String doInBackground(Void... params) {
        DataInputStream inputstream = null;
        ServerSocket serverSocket=null;
        try {
            serverSocket = new ServerSocket(8988);
            Socket client = serverSocket.accept();
            inputstream = new DataInputStream(client.getInputStream());
            String str = inputstream.readUTF();
            serverSocket.close();
            return str;
        } catch (IOException e) {
            return null;
        }
    }


    @Override
    protected void onPostExecute(String result) {
        if(result==null) return;
        String[] separated = result.split(":");

    }
}