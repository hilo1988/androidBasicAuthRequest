package com.example.hilo.authtest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView textView;

    private RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(onClickButton);
        queue = Volley.newRequestQueue(this);

    }



    private View.OnClickListener onClickButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    String msg = String.format("error occurred. see log. exception:[%s]", error);
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, error.getMessage(), error);
                    textView.setText(msg);
                }
            };

            AuthRequest request = new AuthRequest(MainActivity.this, errorListener);
            queue.add(request);
        }
    };

    private Map<String, String> createAuthHeaders() {
        // HTTPのヘッダに認証情報を追加する
        String authValue = String.format("%s:%s", getString(R.string.userName), getString(R.string.password));
        final String encoded = new String(Base64.encode(
                authValue.getBytes(), Base64.DEFAULT));
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + encoded);
        return headers;
    }



    public class AuthRequest extends Request<MyResponse> {
        private final Context context;
        public AuthRequest(Context context, Response.ErrorListener listener) {
            super(Method.GET, getString(R.string.url), listener);
            this.context = context;
        }

        @Override
        protected Response<MyResponse> parseNetworkResponse(NetworkResponse response) {
            String value = new String(response.data);
            return Response.success(new MyResponse(value, response.headers, response.statusCode), HttpHeaderParser.parseCacheHeaders(response));
        }

        @Override
        protected void deliverResponse(MyResponse response) {
            String msg = String.format("request success. result:[%s]", response.toString());
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            textView.setText(msg);
            Log.d(TAG, response.toString());
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            return createAuthHeaders();
        }
    }

    private static class MyResponse {
        final String response;
        final Map<String, String> headers;
        final int status;

        public MyResponse(String response, Map<String, String> headers, int status) {
            this.response = response;
            this.headers = headers;
            this.status = status;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("response:[").append(response).append("]")
                    .append("\n")
                    .append(String.format(Locale.JAPAN,"status:[%d]\n", status));


            sb.append("headers...\n");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                sb.append(String.format("key:[%s] value:[%s]\n", entry.getKey(), entry.getValue()));
            }
            return sb.toString();
        }
    }
}
