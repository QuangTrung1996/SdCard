package com.trung.sdcard;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;

    private static final String PATH_SD_CARD = "PATH_SD_CARD";
    private static final String MY_PREF_PATH = "MyPathSdCard";
    private static final String fileName = "SampleFile.txt";
    private String folderName = "MyFileStorage";
    File myExternalFile;
    String myData = "";

    TextView txtPath;
    EditText inputText;
    Button btnSave, btnRead, btnSetPathDefault, btnSetPathNew, btnSetPathOld;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtPath = findViewById(R.id.txt_path);
        inputText = findViewById(R.id.edit_content);

        btnSave = findViewById(R.id.btn_save);
        btnRead = findViewById(R.id.btn_read);
        btnSetPathNew = findViewById(R.id.btn_set_path_new);
        btnSetPathOld = findViewById(R.id.btn_set_path_old);
        btnSetPathDefault = findViewById(R.id.btn_path_default);

        // kiểm tra bộ nhớ ngoài có sẵn sàng hay không
        checkExternalStorage();

        btnSetPathDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPathDefault();
            }
        });
        btnSetPathOld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPathOld();
            }
        });
        btnSetPathNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPathNew();
            }
        });
        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readFile();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFile();
            }
        });
    }

    /**
     * kiểm tra bộ nhớ ngoài có sẵn sàng hay không
     */
    private void checkExternalStorage() {
        // không sẵn sàng nên không thể lưu
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            btnSave.setEnabled(false);
        }
        else {
            // path mặc định ban đầu
            myExternalFile = new File(getExternalFilesDir(folderName), fileName);
            txtPath.setText(myExternalFile.getAbsolutePath());
        }
    }

    /**
     * Kiểm tra xem bộ nhớ ngoài có sẵn để đọc ít nhất không
     * @return true nếu có, và false ngược lại
     */
    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    /**
     *  Kiểm tra xem bộ nhớ ngoài có sẵn để đọc và ghi không
     *  @return true nếu có, và false ngược lại
     */
    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    /**
     * lưu file với nội dung đã nhập tới vị trí mặc định hoặc vị trí chọn
     */
    private void saveFile() {
        try {
            FileOutputStream fos = new FileOutputStream(myExternalFile);
            fos.write(inputText.getText().toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        inputText.setText("");
    }

    /**
     * đọc file với nội dung đã nhập tới vị trí mặc định hoặc vị trí chọn
     */
    private void readFile() {

        try {
            FileInputStream fis = new FileInputStream(myExternalFile);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                myData = strLine;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        inputText.setText(myData);
    }

    /**
     * Lấy vị trí thư mục mới để sử lý file
     */
    private void setPathNew() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        intent.putExtra("android.content.extra.FANCY", true);
        intent.putExtra("android.content.extra.SHOW_FILESIZE", true);
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                String path = FileUtil.getFullPathFromTreeUri(uri,this);

                txtPath.setText("Path: " + path);

                // lưu lại path cho lần dùng sau
                SharedPreferences pref = getApplicationContext().getSharedPreferences(MY_PREF_PATH, 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(PATH_SD_CARD,path);
                editor.apply();

                myExternalFile = new File(getExternalFilesDir(path), fileName);
            }
        }
    }

    /**
     * Lấy vị trí thư mục đã chọn lần trước để sử lý file
     */
    private void setPathOld() {
        SharedPreferences sharedpreferences = getSharedPreferences(MY_PREF_PATH, Context.MODE_PRIVATE);

        if (sharedpreferences.contains(PATH_SD_CARD)) {
            String path = sharedpreferences.getString(PATH_SD_CARD, "");
            txtPath.setText(path);

            myExternalFile = new File(getExternalFilesDir(path), fileName);
        }
    }

    /**
     * Lấy vị trí thư mục mặc định để sử lý file
     */
    private void setPathDefault() {
        myExternalFile = new File(getExternalFilesDir(folderName), fileName);
        txtPath.setText(myExternalFile.getPath());
    }
}

final class FileUtil {
    static String TAG="TAG";
    private static final String PRIMARY_VOLUME_NAME = "primary";

    @Nullable
    public static String getFullPathFromTreeUri(@Nullable final Uri treeUri, Context con) {
        if (treeUri == null) return null;
        String volumePath = getVolumePath(getVolumeIdFromTreeUri(treeUri),con);
        if (volumePath == null) return File.separator;
        if (volumePath.endsWith(File.separator))
            volumePath = volumePath.substring(0, volumePath.length() - 1);

        String documentPath = getDocumentPathFromTreeUri(treeUri);
        if (documentPath.endsWith(File.separator))
            documentPath = documentPath.substring(0, documentPath.length() - 1);

        if (documentPath.length() > 0) {
            if (documentPath.startsWith(File.separator))
                return volumePath + documentPath;
            else
                return volumePath + File.separator + documentPath;
        }
        else return volumePath;
    }


    @SuppressLint("ObsoleteSdkInt")
    private static String getVolumePath(final String volumeId, Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return null;
        try {
            StorageManager mStorageManager =
                    (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getUuid = storageVolumeClazz.getMethod("getUuid");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
            Object result = getVolumeList.invoke(mStorageManager);

            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String uuid = (String) getUuid.invoke(storageVolumeElement);
                Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);

                // primary volume?
                if (primary && PRIMARY_VOLUME_NAME.equals(volumeId))
                    return (String) getPath.invoke(storageVolumeElement);

                // other volumes?
                if (uuid != null && uuid.equals(volumeId))
                    return (String) getPath.invoke(storageVolumeElement);
            }
            // not found.
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getVolumeIdFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if (split.length > 0) return split[0];
        else return null;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getDocumentPathFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if ((split.length >= 2) && (split[1] != null)) return split[1];
        else return File.separator;
    }
}
