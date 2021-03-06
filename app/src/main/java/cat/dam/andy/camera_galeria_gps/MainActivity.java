package cat.dam.andy.camera_galeria_gps;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ImageView;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.ColorOverlaySubfilter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.graphics.Matrix;
import android.widget.ImageView;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.ColorOverlaySubfilter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //Cridem la llibreria per el filtre
    static
    {
        System.loadLibrary("NativeImageProcessor");
    }

    public static String colors;

    //Declarem variables
    String currentPhotoPath;
    ImageView iv_imatge;
    Button btn_foto, btn_rotar, btn_galeria, btn_filtre;
    Uri uriPhotoImage;
    ContentValues values;

    private ActivityResultLauncher<Intent> activityResultLauncherGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                //here we will handle the result of our intent
                if (result.getResultCode() == Activity.RESULT_OK) {
                    //image picked
                    //get uri of image
                    Intent data = result.getData();
                    Uri imageUri = data.getData();
                    System.out.println("galeria: "+imageUri);
                    iv_imatge.setImageURI(imageUri);
                } else {
                    //cancelled
                    Toast.makeText(MainActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                }
            }
    );
    private ActivityResultLauncher<Intent> activityResultLauncherPhoto = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                //here we will handle the result of our intent
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();
                    iv_imatge.setImageURI(uriPhotoImage); //Amb par??metre EXIF podem canviar orientaci?? (per defecte horiz en versions android antigues)
                    refreshGallery();//refresca gallery per veure nou fitxer
                        /* Intent data = result.getData(); //si volguessim nom??s la miniatura
                        Uri imageUri = data.getData();
                        iv_imatge.setImageURI(imageUri);*/
                } else {
                    //cancelled
                    Toast.makeText(MainActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                }




            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Declarem la foto
        iv_imatge = findViewById(R.id.iv_foto);

        //Declarem els valors
        btn_foto = findViewById(R.id.btn_foto);
        btn_galeria = findViewById(R.id.btn_galeria);
        btn_rotar = findViewById(R.id.btn_rotar);
        btn_filtre = findViewById(R.id.btn_filtre);

        colors = "SF";
        //Cridem el boto per obrir la galeria
        btn_galeria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (checkPermissions()) {
                        openGallery();
                    } else {
                        askForPermissions();
                    }
            }
        });

        //Cridem el boto per obrir la camara
        btn_foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (checkPermissions())
                            {
                                takePicture();
                    } else {
                        askForPermissions();
                    }
            }
        });

        //Cridem el boto per posar un filtre
        btn_filtre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions())
                {
                    posarfiltre();
                } else {
                    askForPermissions();
                }
            }
        });

        //Boto que rotara la imatge
        btn_rotar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    rotarimatge();
            }
        });

    }

    private boolean checkPermissions() {
        return (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        );
    }


    private void askForPermissions() {
        ActivityCompat.requestPermissions(this, new String[]
                    {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        }, 3);
    }

    private void openGallery() {
        try {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    activityResultLauncherGallery.launch(Intent.createChooser(intent, "Select File"));
                } else {
                    Toast.makeText(MainActivity.this, "El seu dispositiu no permet accedir a la galeria",
                            Toast.LENGTH_SHORT).show();
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void takePicture() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(MainActivity.this, "Error en la creaci?? del fitxer",
                        Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "La meva foto");
                values.put(MediaStore.Images.Media.DESCRIPTION, "Foto feta el " + System.currentTimeMillis());
                Uri uriImage = FileProvider.getUriForFile(this,
                        this.getPackageName()+ ".provider", //(use your app signature + ".provider" )
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImage);
                    activityResultLauncherPhoto.launch(intent);
                } else {
                    Toast.makeText(MainActivity.this, "No s'ha pogut crear la imatge",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "El seu dispositiu no permet accedir a la c??mera",
                        Toast.LENGTH_SHORT).show();
            }
    }

    public void posarfiltre() {

        //Creem el filtre cada cop que apreti canviara el filtre
        if(colors.equals("SF")){
            iv_imatge.setColorFilter((-10000000), PorterDuff.Mode.LIGHTEN);
            colors = "LOWGRY";
        } else if(colors.equals("LOWGRY")){
            iv_imatge.setColorFilter(Color.DKGRAY, PorterDuff.Mode.LIGHTEN);
            colors = "DKGRAY";
        } else if(colors.equals("DKGRAY")){
            iv_imatge.setColorFilter(Color.GRAY, PorterDuff.Mode.LIGHTEN);
            colors = "GRAY";
        } else if(colors.equals("GRAY")){
            iv_imatge.setColorFilter(Color.LTGRAY, PorterDuff.Mode.LIGHTEN);
            colors = "LTGRAY";
        } else if(colors.equals("LTGRAY")){
            iv_imatge.setColorFilter(Color.RED, PorterDuff.Mode.LIGHTEN);
            colors = "RED";
        } else if(colors.equals("RED")){
            iv_imatge.setColorFilter((-11000000), PorterDuff.Mode.LIGHTEN);
            colors = "BROWN";
        } else if(colors.equals("BROWN")){
            iv_imatge.setColorFilter(Color.BLACK, PorterDuff.Mode.LIGHTEN);
            colors = "SF";
        }


    }

    public void rotarimatge(){
        if (iv_imatge.getRotation() == 0 || iv_imatge.getRotation() == 360 ){
            iv_imatge.setRotation(90);
        }else if (iv_imatge.getRotation() == 90) {
            iv_imatge.setRotation(180);
        }else if (iv_imatge.getRotation() == 180){
            iv_imatge.setRotation(270);
        }else if (iv_imatge.getRotation() == 270){
            iv_imatge.setRotation(360);
        }

    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // File storageDir = getFilesDir();//no es veur?? a la galeria
        // File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES+File.separator+this.getPackageName());//No es veur?? a la galeria
        File storageDir =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES+File.separator+this.getPackageName());
        //NOTE: MANAGE_EXTERNAL_STORAGE is a special permission only allowed for few apps like Antivirus, file manager, etc. You have to justify the reason while publishing the app to PlayStore.
        if (!storageDir.exists())
        {
            storageDir.mkdir();
        }
        storageDir.mkdirs();
        System.out.println("storageDir: "+storageDir);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Save a file: path for use with ACTION_VIEW intents
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        uriPhotoImage = Uri.fromFile(image);
        System.out.println("fitxer: "+uriPhotoImage);
        return image;
    }

    private void refreshGallery() {
        //Cal refrescar per poder veure la foto creada a la galeria
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            //Uri contentUri = Uri.fromFile(uriPhotoImage); // out is your output file
            mediaScanIntent.setData(uriPhotoImage);
            this.sendBroadcast(mediaScanIntent);
        } else {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }
}
