package com.vanvatcorporation.doubleclips.activities;

import static com.vanvatcorporation.doubleclips.FFmpegEdit.runAnyCommand;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.arthenica.ffmpegkit.Log;
import com.arthenica.ffmpegkit.Statistics;
import com.google.gson.Gson;
import com.vanvatcorporation.doubleclips.AdsHandler;
import com.vanvatcorporation.doubleclips.BuildConfig;
import com.vanvatcorporation.doubleclips.FFmpegEdit;
import com.vanvatcorporation.doubleclips.R;
import com.vanvatcorporation.doubleclips.UncaughtExceptionHandler;
import com.vanvatcorporation.doubleclips.activities.main.MainAreaScreen;
import com.vanvatcorporation.doubleclips.activities.main.ProfileAreaScreen;
import com.vanvatcorporation.doubleclips.activities.main.TemplateAreaScreen;
import com.vanvatcorporation.doubleclips.constants.Constants;
import com.vanvatcorporation.doubleclips.ext.rajawali.RajawaliExample;
import com.vanvatcorporation.doubleclips.helper.CompressionHelper;
import com.vanvatcorporation.doubleclips.helper.DateHelper;
import com.vanvatcorporation.doubleclips.helper.IOHelper;
import com.vanvatcorporation.doubleclips.helper.IOImageHelper;
import com.vanvatcorporation.doubleclips.helper.ImageHelper;
import com.vanvatcorporation.doubleclips.helper.NotificationHelper;
import com.vanvatcorporation.doubleclips.helper.ProgressCompressionHelper;
import com.vanvatcorporation.doubleclips.helper.StringFormatHelper;
import com.vanvatcorporation.doubleclips.impl.AppCompatActivityImpl;
import com.vanvatcorporation.doubleclips.impl.NavigationIconLayout;
import com.vanvatcorporation.doubleclips.impl.ViewPagerImpl;
import com.vanvatcorporation.doubleclips.impl.java.RunnableImpl;
import com.vanvatcorporation.doubleclips.impl.java.RunnableImpl2;
import com.vanvatcorporation.doubleclips.manager.LoggingManager;
import com.vanvatcorporation.doubleclips.popups.CompressionPopup;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivityImpl {



    ViewPagerImpl viewPager;


    MainAreaScreen homeAreaScreen;
    TemplateAreaScreen templateAreaScreen;
    ProfileAreaScreen profileAreaScreen;





    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));


        // Notification Stuff
        NotificationHelper.createNotificationChannel(this);



        // Initialize Mobile Ads SDK
        AdsHandler.initializeAds(this, this);



//        pickButton = findViewById(R.id.button);
//        pickButton.setOnClickListener(v -> pickingContent());
//
//        trimButton = findViewById(R.id.button2);
//        trimButton.setOnClickListener(v -> FFmpegMethod.trimVideo(this, pathText.getText().toString(), outputText.getText().toString(), 0, 5));
//
//        pathText = findViewById(R.id.pathText);
//        outputText = findViewById(R.id.outputText);
//
//        errorText = findViewById(R.id.textView);


        // Check and request permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }




        // Start initializing critical modules ----------------------------------------------------------


        findViewById(R.id.navigationElement1).setOnClickListener(v -> {
            viewPager.setCurrentItem(0, true);
        });
        findViewById(R.id.navigationElement2).setOnClickListener(v -> {
            viewPager.setCurrentItem(1, true);
        });
        findViewById(R.id.navigationElement3).setOnClickListener(v -> {
            viewPager.setCurrentItem(2, true);
        });
        findViewById(R.id.navigationElement4).setOnClickListener(v -> {
            viewPager.setCurrentItem(3, true);
        });
        findViewById(R.id.navigationElement5).setOnClickListener(v -> {
            viewPager.setCurrentItem(4, true);
        });




        homeAreaScreen = (MainAreaScreen) getLayoutInflater().inflate(R.layout.pager_main_homepage, null);
        templateAreaScreen = (TemplateAreaScreen) getLayoutInflater().inflate(R.layout.pager_main_template, null);
        View View3 = getLayoutInflater().inflate(R.layout.pager_main_search, null);
        View View4 = getLayoutInflater().inflate(R.layout.pager_main_storage, null);
        profileAreaScreen = (ProfileAreaScreen) getLayoutInflater().inflate(R.layout.pager_main_profile, null);


        View belowNavigationBar = findViewById(R.id.belowNavigationBar);

        View navigationButton1 = belowNavigationBar.findViewById(R.id.navigationElement1);
        View navigationButton2 = belowNavigationBar.findViewById(R.id.navigationElement2);
        View navigationButton3 = belowNavigationBar.findViewById(R.id.navigationElement3);
        View navigationButton4 = belowNavigationBar.findViewById(R.id.navigationElement4);
        View navigationButton5 = belowNavigationBar.findViewById(R.id.navigationElement5);

        View[] navigationButtons = {navigationButton1, navigationButton2, navigationButton3, navigationButton4, navigationButton5};





        ((NavigationIconLayout)navigationButton1).runAnimation(NavigationIconLayout.AnimationType.SELECTED);


        viewPager = findViewById(R.id.mainViewPager);
        viewPager.insertView(homeAreaScreen, templateAreaScreen, View3, View4, profileAreaScreen);
        viewPager.setupActions(
                new RunnableImpl2() {
                    @Override
                    public <T, T2> void runWithParam(T param, T2 param2) {
                        int lastPosition = (int) param;
                        int position = (int) param2;


                        ((NavigationIconLayout)navigationButtons[position]).runAnimation(NavigationIconLayout.AnimationType.SELECTED);
                        ((NavigationIconLayout)navigationButtons[lastPosition]).runAnimation(NavigationIconLayout.AnimationType.UNSELECTED);

                    }
                }
        );

        // ---------------------------------------------------------- End initializing critical modules


        homeAreaScreen.reloadingProject();




        homeAreaScreen.filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();


                        CompressionPopup dialog = new CompressionPopup(this, getString(R.string.alert_processing_decompression), "Extracting project...");
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            ProgressCompressionHelper.unzipFolder(this, getContentResolver(), uri, Constants.DEFAULT_PROJECT_DIRECTORY(this),
                            new ProgressCompressionHelper.UnzipProgressListener() {

                                @Override
                                public void onProgress(long bytesExtracted, long totalBytes, String name) {
                                    int percent = (int)((bytesExtracted * 100) / totalBytes);

                                    dialog.previewProgressBar.post(() -> {
                                        dialog.previewProgressBar.setMax(100);
                                        dialog.previewProgressBar.setProgress(percent);
                                        dialog.processingPercent.setText(percent + "%");
                                        dialog.descriptionText.setText("Extracting project... " + name);
                                    });
                                }

                                @Override
                                public void onCompleted() {
                                    dialog.dialog.dismiss();
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                        });

                    }
                }
        );

        homeAreaScreen.fileCreatorLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();

                        CompressionPopup dialog = new CompressionPopup(this, getString(R.string.alert_processing_compression), "Compressing project...");
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            if(homeAreaScreen.currentExportingProject == null) return;
                            ProgressCompressionHelper.zipFolder(this, homeAreaScreen.currentExportingProject.getProjectPath(), getContentResolver(), uri, new ProgressCompressionHelper.ZipProgressListener() {
                                @Override
                                public void onProgress(long bytesWritten, long totalBytes, String name) {
                                    int percent = (int)((bytesWritten * 100) / totalBytes);

                                    dialog.previewProgressBar.post(() -> {
                                        dialog.previewProgressBar.setMax(100);
                                        dialog.previewProgressBar.setProgress(percent);
                                        dialog.processingPercent.setText(percent + "%");
                                        dialog.descriptionText.setText("Compressing project... " + name);
                                    });
                                }

                                @Override
                                public void onCompleted() {
                                    dialog.dialog.dismiss();
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                            homeAreaScreen.currentExportingProject = null;
                        });

                    }
                }
        );
    }





    @Override
    protected void onResume() {
        super.onResume();
        AdsHandler.displayThanksForShowingAds(this);
//        AdsHandler.loadBothAds(this, this);
    }












    public static String getPath(Context context, Uri uri) {
        String[] projection = { MediaStore.Video.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(projection[0]);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return uri.getPath();
    }









    public static class MigrationHelper {
        public static void migrate(MainAreaScreen.ProjectData data)
        {
            // List some critical changes need to be apply e.g a null field that wasn't exist until
            // the newer version come out, and loading it in the new version crashes the app
            if(data.version == null)
                data.version = BuildConfig.VERSION_NAME;
            switch (data.version)
            {
                case "1.0.0":
                    // Migrate for the lower project
                    // For example newer version may create the folder
                    // but older version don't, therefore missing folder leading to ffmpeg failure
                    // IOHelper.writeToFileAsRaw(MainActivity.this, "/file-or-folder/to/create/for/migration");
                    break;
            }
        }
    }

}
