package com.example.moshea.remotecontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    public void btnAddText(View view)
    {
         EditText ip = (EditText) findViewById(R.id.txtIPAddress);
         new Messenger().execute("Add",ip.getText().toString());
    }

    public void btnReplaceText(View view)
    {
        EditText ip = (EditText) findViewById(R.id.txtIPAddress);
        new Messenger().execute("Replace",ip.getText().toString());
    }

    public void btnClearText(View view)
    {
        EditText ip = (EditText) findViewById(R.id.txtIPAddress);
        new Messenger().execute("Clear",ip.getText().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            final EditText txtIPAddress = (EditText) rootView.findViewById(R.id.txtIPAddress);

            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            String ip = sharedPref.getString("IP",null);

            if (ip != null)
            {
                txtIPAddress.setText(ip);
            }

            txtIPAddress.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
                @Override
                public void afterTextChanged(Editable s) {
                    SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("IP", s.toString());
                    editor.commit();
                }
            });

            return rootView;
        }

    }

    public class Messenger extends AsyncTask<String, Void, Void>
    {

        @Override
        protected Void doInBackground(String... params) {

            try
            {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(params[1]);
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.queueDeclare("RemoteControl", false, false, false, null);
                String message = params[0];
                channel.basicPublish("", "RemoteControl", null, message.getBytes());
                channel.close();
                connection.close();
            } catch (IOException e) {
            }


            return null;
        }
    }
}
