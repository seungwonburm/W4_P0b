package com.example.w4_p0b;

import static java.security.AccessController.getContext;


import android.Manifest;

import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Switch flashlight;
    private TextView tv;
    private boolean yesFlash, permission=false;
    private SearchView searchView;
    private ListView listView;

    ArrayList<String> list;
    ArrayAdapter<String> arrayAdapter;

    Camera.Parameters params;
    GestureDetector gd;
    Camera camera;
    CameraManager camManager, cm;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flashlight = (Switch) findViewById(R.id.flashlight);
        searchView = (SearchView) findViewById(R.id.search);

//        listView.setAdapter(arrayAdapter);


        searchView.clearFocus();
//        list = new ArrayList<>();
//        list.add("ON");
//        list.add("on");
//        list.add("OFF");
//        list.add("off");


        searchView.setQueryHint("Type ON or OFF");

        yesFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        gd = new GestureDetector(this, new Gestures());
        camManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);



        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (yesFlash){
                openCamera();
                if (isFlashOn() == true){
                    flashlight.setChecked(true);
                }
            }
        }


       flashlight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean changed) {
                if (changed){
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        if (yesFlash){
                            openCamera();
                            turnOnFlash();
                        }else{
                            Toast.makeText(getApplicationContext(), "No Flashlight Installed on your device", Toast.LENGTH_LONG).show();
                            flashlight.setChecked(false);
                        }
                    }
                    else {

                        permissionCheck();

                        if (permission){
                            try{
                                String cameraId = camManager.getCameraIdList()[0];
                                camManager.setTorchMode(cameraId, true);
                            } catch (CameraAccessException e){
                                Toast.makeText(getApplicationContext(), "No Flashlight Installed on your device", Toast.LENGTH_LONG).show();
                                flashlight.setChecked(false);
                            }
                        }else {
                            Toast.makeText(getApplicationContext(), "Requires Permission!", Toast.LENGTH_LONG).show();
                            flashlight.setChecked(false);
                        }

                    }

                } else {

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        if (yesFlash){
                            turnOffFlash();
                        }

                    } else {
                        if (permission){
                            try{
                                String cameraId = camManager.getCameraIdList()[0];
                                camManager.setTorchMode(cameraId, false);
                            } catch (CameraAccessException e){
                                Toast.makeText(getApplicationContext(), "No Flashlight Installed on your device", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Requires Permission!", Toast.LENGTH_LONG).show();
                        }


                    }
                }
            }



        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (s.equalsIgnoreCase("ON")){

                    flashlight.setChecked(true);
                }
                else if (s.equalsIgnoreCase("OFF") ){
                    flashlight.setChecked(false);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });



    }



    public boolean permissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int resultA = checkCallingOrSelfPermission(Manifest.permission.CAMERA);
            if (resultA == PackageManager.PERMISSION_GRANTED) {
                permission = true;
                return permission;

            } else {

                requestPermissions(new String[]{Manifest.permission.CAMERA}, 42);

                if (resultA == PackageManager.PERMISSION_DENIED) {
                    permission = false;
                    return permission;
                } else {
                    permission = true;
                    return permission;
                }

            }
        }
        return permission;
    }

    public void openCamera(){
        if (camera == null){
            try{
                camera = Camera.open();
                params = camera.getParameters();

            } catch (Exception e){
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void turnOnFlash(){
        Camera.Parameters params = camera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(params);
        camera.startPreview();
        Toast.makeText(getApplicationContext(), "On!", Toast.LENGTH_LONG).show();
    }

    public void turnOffFlash(){
        Camera.Parameters params = camera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(params);
        camera.stopPreview();
        Toast.makeText(getApplicationContext(), "Off!", Toast.LENGTH_LONG).show();

    }

    public boolean isFlashOn() {
        Camera.Parameters parameters = camera.getParameters();
        return parameters.getFlashMode() == "FLASH_MODE_TORCH";
    }



    private class Gestures extends GestureDetector.SimpleOnGestureListener {


        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float v, float v1) {
                if (Math.abs(event1.getY() - event2.getY()) > 90) {
                    if (event1.getY() - event2.getY() > 40) {
                        flashlight.setChecked(true);
                        Toast.makeText(getApplicationContext(), "Fling Up", Toast.LENGTH_LONG).show();
                    }
                    else if (event2.getY() - event1.getY() > 40) {
                        flashlight.setChecked(false);
                        Toast.makeText(getApplicationContext(), "Fling Down", Toast.LENGTH_LONG).show();
                    }

                }
                return super.onFling(event1, event2, v, v1);
            }
        }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gd.onTouchEvent(event);
        return super.onTouchEvent(event);
    }


}