package com.example.slotgacor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity{
    private ImageView img1;
    private ImageView img2;
    private ImageView img3;
    ArrayList<String> imageUrl = new ArrayList<>();
    Button _play;
    boolean _isStarted = false;
    Wheel _Wheel1, _Wheel2, _Wheel3;
    ExecutorService _rollExecService, _imageExecService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img1 = findViewById(R.id.img_slot1);
        img2 = findViewById(R.id.img_slot2);
        img3 = findViewById(R.id.img_slot3);
        _play = findViewById(R.id.btn_get);
        _imageExecService = Executors.newSingleThreadExecutor();
        _rollExecService = Executors.newFixedThreadPool(3);

        _Wheel1 = new Wheel(img1);
        _Wheel2 = new Wheel(img2);
        _Wheel3 = new Wheel(img3);

        _imageExecService.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    final String txt = loadStringFromNetwork("https://mocki.io/v1/821f1b13-fa9a-43aa-ba9a-9e328df8270e");
                    try{
                        JSONArray jsonArray = new JSONArray(txt);
                        for(int i = 0; i < jsonArray.length(); i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            imageUrl.add(jsonObject.getString("url"));
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                };
            }
        });

        _play.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(v.getId() == _play.getId()){
                    if(!_isStarted){
                        _Wheel1._isStarted = true;
                        _Wheel2._isStarted = true;
                        _Wheel3._isStarted = true;

                        _rollExecService.execute(_Wheel1);
                        _rollExecService.execute(_Wheel2);
                        _rollExecService.execute(_Wheel3);

                        _play.setText("Stop");

                    } else {
                        _Wheel1._isStarted = false;
                        _Wheel2._isStarted = false;
                        _Wheel3._isStarted = false;

                        _play.setText("Start");
                    }
                    _isStarted = !_isStarted;
                }
            }
        });
    }

    private String loadStringFromNetwork(String s) throws IOException {
        final URL myUrl = new URL(s);
        final InputStream in = myUrl.openStream();

        final StringBuilder out = new StringBuilder();
        final byte[] buffer = new byte[1024];

        try{
            for(int ctr; (ctr = in.read(buffer)) != -1; ){
                out.append(new String(buffer, 0, ctr));
            }
        } catch (IOException e){
            throw new RuntimeException("Gagal mendapatkan text", e);
        }

        return out.toString();
    }

    class Wheel implements Runnable {
        Handler handler = new Handler(Looper.getMainLooper());
        ImageView _slotImage;
        Random _random = new Random();
        public boolean _isStarted;
        int i;

        public Wheel(ImageView _slotImage){
            this._slotImage = _slotImage;
            i = 0;
            _isStarted = true;
        }

        @Override
        public void run() {
            while(_isStarted){
                i = _random.nextInt(3);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(imageUrl.get(i)).into(_slotImage);
                    }
                });

                try{
                    Thread.sleep(_random.nextInt(500));
                } catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }
}