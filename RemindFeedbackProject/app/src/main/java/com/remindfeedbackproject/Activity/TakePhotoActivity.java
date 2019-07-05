package com.remindfeedbackproject.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.remindfeedbackproject.Dto.MemberDto;
import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Dao.MemberDao;
import com.remindfeedbackproject.Dto.FolderDto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TakePhotoActivity extends AppCompatActivity {
    //intent 객체 선언
    String loginEmailKey;
    String activityState;
    String[] intentData;
    int position;

    //카메라와 갤러리 관련 변수 선언
    private final int CAMERA_CODE = 150; //카메라 코드
    private final int GALLERY_CODE = 151; //갤러리 코드
    private final int CROP_IMAGE = 152; //크롭 코드

    //사진 파일을 저장하기 위한 경로 지정
    private File tempPhotoFile = null;
    private Uri photoUri; //카메라 앱 사진 Uri
    private String time; //사진 파일 이름을 정하기 위한 현재시간 변수

    //activity로 넘겨줄 제목과 내용을 저장할 변수 선언
    private String newTitle = null;
    private String newBitmapPath = null;

    //버튼 혹은 텍스트 객체 변수 선언
    private TextView txtName; //다이얼로그 이름
    private EditText etxtTitle; //게시글 제목
    private ImageView imgContent; //게시글 사진
    private Button btnGallery; //갤러리 버튼
    private Button btnCamera; //카메라 버튼
    private Button btnCancel; //취소 버튼
    private Button btnCreate; //생성 버튼

    //Activity를 최초 화면에 불러올 때 실행하는 메소드
    //여기서 사용할 변수들을 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.takephoto_activity);

        //intent값 저장
        loginEmailKey = getIntent().getStringExtra("loginEmailKey");
        activityState = getIntent().getStringExtra("activityState");

        //사용자 입력이 있을 객체를 초기화
        //버튼과 텍스트 객체를 초기화
        txtName = (TextView) findViewById(R.id.takephoto_txt_name);
        etxtTitle = (EditText) findViewById(R.id.takephoto_etxt_title);
        imgContent = (ImageView) findViewById(R.id.takephoto_img_content);
        btnGallery = (Button) findViewById(R.id.takephoto_btn_gallery);
        btnCamera = (Button) findViewById(R.id.takephoto_btn_camera);
        btnCancel = (Button) findViewById(R.id.takephoto_btn_cancel);
        btnCreate = (Button) findViewById(R.id.takephoto_btn_create);

        //수정을 위해 takephoto activity를 호출한 경우
        if (activityState.equals("update")) {
            intentData = getIntent().getStringArrayExtra("intentData");
            position = getIntent().getIntExtra("position", -1);

            txtName.setText("게시글 수정"); //다이얼로그 이름 변경
            etxtTitle.setText(intentData[0]); //기존의 제목 집어넣음

            //이미지 파일의 경로에서 이미지를 불러와 적용
            newBitmapPath = intentData[1];
            Bitmap bitmap = BitmapFactory.decodeFile(newBitmapPath);
            imgContent.setImageBitmap(bitmap);//기존의 내용 집어넣음
            btnCreate.setText("수정");//추가 버튼의 텍스트를 "수정"으로 변경
        }
    }

    //View를 화면에 완전히 불러온 후 실행하는 메소드
    //사용자가 화면과 상호작용을 할 때 일어나는 일에 대한 작업을 함
    //편집 화면이므로 이메일로 친구 찾기, 이름/상태 메세지 수정 등을 할 수 있음
    @Override
    public void onResume() {
        super.onResume();

        //갤러리에서 이미지를 가져오기 위해 버튼을 누르는 경우
        //버튼 이벤트를 받는 리스너
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //사진을 갤러리에서 가져오는 메소드 호출
                selectGallery();
            }
        });

        //카메라에서 이미지를 가져오기 위해 버튼을 누르는 경우
        //버튼 이벤트를 받는 리스너
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //사진을 카메라에서 가져오는 메소드 호출
                selectPhoto();
            }
        });

        //취소 버튼을 누르는 경우
        //버튼 이벤트를 받는 리스너
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //생성 혹은 수정 버튼을 누르는 경우
        //버튼 이벤트를 받는 리스너
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //게시글에 공백이 있으면 액티비티를 완료하지 못하게 함
                //제목이 공백인 경우
                if (etxtTitle.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "게시글 제목을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    etxtTitle.requestFocus(); //제목에 포커스를 맞춤
                    return;
                }
                //갤러리 혹은 카메라에서 사진을 가져오지 않은 경우
                if (newBitmapPath == null) {
                    Toast.makeText(getApplicationContext(), "갤러리 혹은 카메라에서 사진을 첨부해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //fragment2로 값을 전달하는 메소드 호출
                String[] photoData = new String[2];
                newTitle = etxtTitle.getText().toString();
                photoData[0] = newTitle;
                photoData[1] = newBitmapPath;

                Intent intent = new Intent();
                intent.putExtra("intentData", photoData);
                if (activityState.equals("update") && position != -1) {
                    intent.putExtra("position", position);
                }
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    //앨범으로 가는 메소드
    private void selectGallery() {
        Intent intent = new Intent();
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);

        startActivityForResult(intent, GALLERY_CODE);
    }

    //카메라로 가는 메소드
    private void selectPhoto() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            //Intent로 카메라 호출
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                tempPhotoFile = null;
                try {
                    tempPhotoFile = createImageFile();
                } catch (IOException ex) {

                }
                if (tempPhotoFile != null) {
                    photoUri = FileProvider.getUriForFile(this, getPackageName(), tempPhotoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(intent, CAMERA_CODE);
                }
            }
        }
    }

    //이미지 생성
    private File createImageFile() throws IOException {
        MemberDao memberDao = new MemberDao(TakePhotoActivity.this);
        FolderDto folderDto = memberDao.readMemberDataPathInfo(loginEmailKey);

        //경로 지정
        setPhotoDate();//파일 명을 정하기 위해 현재 시간 구함
        String photoPath = folderDto.getPhotoDirectoryPath();//파일 경로 지정
        String photoTitle = "MyPhoto_" + time + ".png"; //파일 이름 지정
        newBitmapPath = photoPath + "/" + photoTitle;

        File photo = new File(photoPath + "/", photoTitle);

        return photo;
    }

    //현재 날짜를 저장하는 메소드
    private void setPhotoDate() {
        long nowTime = System.currentTimeMillis(); //현재 시간을 시스템에서 가져옴
        Date date = new Date(nowTime); //Date 생성하기
        SimpleDateFormat timeSDF = new SimpleDateFormat("yyyyMMdd_hhmmss");//yyyy.MM.dd a hh:mm:ss 형식으로 가져옴

        time = timeSDF.format(date); //시간을 time에 저장
    }

    //호출한 액티비티로부터 결과값을 받아와 그에 따른 이벤트를 수행하는 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY_CODE:
                    cropImage(data.getData());
                    break;
                case CAMERA_CODE:
                    cropImage(photoUri);
                    break;
                case CROP_IMAGE:
                    getPictureForPhoto(); //카메라에서 가져오기
                    break;
            }
        }
    }

    private void cropImage(Uri photoUri) {
        //크롭 후 저장할 Uri
        Uri savingUri = photoUri;
        //갤러리에서 선택한 경우에는 tempFile 이 없으므로 새로 생성
        if (tempPhotoFile == null) {
            try {
                tempPhotoFile = createImageFile();

                //사진을 Bitmap으로 변환
                String uriToPhotoPath = getRealPathFromURI(photoUri);
                Bitmap bitmap = BitmapFactory.decodeFile(uriToPhotoPath);
                //FileOutputStream으로 가져온 사진 데이터 저장
                FileOutputStream fileOutputStream = new FileOutputStream(newBitmapPath);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

                //크롭 후 저장할 Uri
                savingUri = Uri.fromFile(tempPhotoFile);
            } catch (IOException e) {
                Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                finish();
                e.printStackTrace();
            }
        }

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("noFaceDetection", true); //인물 사진도 고정 비율
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, savingUri);
        startActivityForResult(intent, CROP_IMAGE);
    }

    //뒤로가기 버튼을 누르는 경우
    @Override
    public void onBackPressed() {
        //intent 날리고 종료
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);

        super.onBackPressed();
    }

    //카메라로 찍은 사진 적용
    private void getPictureForPhoto() {
        //비트맵 이미지로 가져옴
        Bitmap bitmap = BitmapFactory.decodeFile(newBitmapPath);

        //ExifInterface : 이미지가 가지고 있는 정보의 집합 클래스
        //이미지를 정보에 맞게 회전
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(newBitmapPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation;
        int exifDegree;

        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientationToDegrees(exifOrientation);
        } else {
            exifDegree = 0;
        }
        //이미지 변환
        Bitmap newBitmap = rotate(bitmap, exifDegree);
        //변환된 이미지 사용
        imgContent.setImageBitmap(newBitmap);//이미지 뷰에 비트맵 넣기
    }


    //사진의 회전값 가져오는 메소드
    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    //사진을 정방향으로 회전하는 메소드
    private Bitmap rotate(Bitmap src, float degree) {
        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    //사진의 절대경로 구하는 메소드
    private String getRealPathFromURI(Uri contentUri) {
        int column_index = 0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }

        return cursor.getString(column_index);
    }
}