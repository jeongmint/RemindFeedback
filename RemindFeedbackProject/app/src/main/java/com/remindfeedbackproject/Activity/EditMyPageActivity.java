package com.remindfeedbackproject.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Dao.MemberDao;
import com.remindfeedbackproject.Dto.FolderDto;
import com.remindfeedbackproject.Dto.MemberDto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditMyPageActivity extends AppCompatActivity {
    //intent 값 선언
    String loginEmailKey;

    static final int UPDATE_NICKNAME_REQUEST = 120; // The request code
    static final int UPDATE_PROFILE_MESSAGE_REQUEST = 121;  // The request code

    //카메라와 갤러리 관련 변수 선언
    private final int CAMERA_CODE = 122; //카메라 코드
    private final int GALLERY_CODE = 123; //갤러리 코드
    private final int CROP_IMAGE = 124; //크롭 코드

    //사진 파일을 저장하기 위한 경로 지정
    private File tempPhotoFile = null;
    private Uri photoUri; //카메라 앱 사진 Uri

    //activity로 넘겨줄 제목과 내용을 저장할 변수 선언
    private String newTitle;
    private String newBitmapPath;

    //사용자 입력이 있을 변수 선언
    ImageButton ibtnProfilePhoto; //프로필 사진 수정 버튼 객체
    ImageButton ibtnNickName; //이름(별명) 수정 버튼 객체
    ImageButton ibtnProfileMessage; //프로필 메세지 수정 버튼 객체

    ImageView imgProfilePhoto; //사용자의 프로필 이미지를 보여주는 객체
    TextView txtNickName; //사용자의 이름(별명)을 보여주는 객체
    TextView txtEmail; //사용자의 이메일을 보여주는 객체
    TextView txtProfileMessage; //사용자의 상태 메세지를 보여주는 객체

    //Activity를 최초 화면에 불러올 때 실행하는 메소드
    //여기서 사용할 변수들을 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editmypage_activity);

        //intent 값 저장
        loginEmailKey = getIntent().getStringExtra("loginEmailValue");

        //사용자 입력이 있을 변수 초기화
        ibtnProfilePhoto = (ImageButton) findViewById(R.id.editmypage_ibtn_profilephoto);
        ibtnNickName = (ImageButton) findViewById(R.id.editmypage_ibtn_nickname);
        ibtnProfileMessage = (ImageButton) findViewById(R.id.editmypage_ibtn_profilemessage);

        //Email 값을 기준으로 memberDao 값에서 회원 정보를 가져옴
        //MemberDao에서 이메일, 닉네임, 프로필 사진 경로, 프로필 상태 메세지(안보임) 가져옴
        MemberDao memberDao = new MemberDao(this);
        MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey); //로그인 당시 이메일 키를 통해 회원 정보 가져옴

        //사용자 프로필 객체를 초기화
        imgProfilePhoto = (ImageView) findViewById(R.id.editmypage_img_profilephoto);
        txtNickName = (TextView) findViewById(R.id.editmypage_txt_nickname);
        txtEmail = (TextView) findViewById(R.id.editmypage_txt_email);
        txtProfileMessage = (TextView) findViewById(R.id.editmypage_txt_profilemessage);

        //기본 이미지가 없는 경우
        if (memberDto.getMemberProfilePhotoPath().equals("NONE")) {
            imgProfilePhoto.setImageResource(R.drawable.icon_account_48dp);
        }
        //기본 이미지가 있는 경우
        else {
            Bitmap bitmap = BitmapFactory.decodeFile(memberDto.getMemberProfilePhotoPath());
            imgProfilePhoto.setImageBitmap(bitmap);

            //이미지 버튼 둥글게 만들기
            imgProfilePhoto.setBackground(new ShapeDrawable(new OvalShape()));
            if (Build.VERSION.SDK_INT >= 21) {
                imgProfilePhoto.setClipToOutline(true);
            }
        }

        txtNickName.setText(memberDto.getMemberNickName());
        txtEmail.setText(memberDto.getMemberEmail());
        txtProfileMessage.setText(memberDto.getMemberProfileMessage());

    }

    //Activity를 화면에 완전히 불러온 후 실행하는 메소드
    //사용자가 화면과 상호작용을 할 때 일어나는 일에 대한 작업을 함
    //메인 화면이므로 마이페이지/로그아웃/게시판 생성/게시판에 들어가기 등을 할 수 있음
    @Override
    protected void onResume() {
        super.onResume();

        //프로필 사진 수정을 위해 버튼을 누르는 경우
        //버튼 이벤트를 수행하는 리스너
        ibtnProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //카메라에서 가져올지 갤러리에서 가져올지 다이얼로그 띄움
                confirmProfilePhotoDialog();
            }
        });

        //닉네임 수정을 위해 버튼을 누르는 경우
        //버튼 이벤트를 수행하는 리스너
        ibtnNickName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //edit activity로 화면 전환
                launchEditMyPageToNickNameEditScreen();
            }
        });

        //상태 메세지 수정을 위해 버튼을 누르는 경우
        //버튼 이벤트를 수행하는 리스너
        ibtnProfileMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //edit activity로 화면 전환
                launchEditMyPageToProfileMessageScreen();
            }
        });
    }

    //이름(별명) 편집 화면 호출 메소드
    private void launchEditMyPageToNickNameEditScreen() {
        Intent intent = new Intent(EditMyPageActivity.this, EditActivity.class); //새로운 인텐트 객체 생성
        intent.putExtra("loginEmailValue", loginEmailKey); //이메일 값을 intent로 전달
        intent.putExtra("nickname", txtNickName.getText().toString()); //닉네임 값을 intent로 전달
        intent.putExtra("editState", "nicknameUpdate"); //닉네임 변경을 위한 호출임을 명시
        startActivityForResult(intent, UPDATE_NICKNAME_REQUEST); //intent 호출
    }

    //상태 메세지 편집 화면 호출 메소드
    private void launchEditMyPageToProfileMessageScreen() {

        Intent intent = new Intent(EditMyPageActivity.this, EditActivity.class); //새로운 인텐트 객체 생성
        intent.putExtra("loginEmailValue", loginEmailKey); //이메일 값을 intent로 전달
        intent.putExtra("message", txtProfileMessage.getText().toString()); //상태 메세지 값을 intent로 전달
        System.out.println("EditMyPage의 로그인 이메일 키는 " + loginEmailKey + "입니다.");
        intent.putExtra("editState", "messageUpdate"); //상태 메세지을 위한 호출임을 명시
        startActivityForResult(intent, UPDATE_PROFILE_MESSAGE_REQUEST); //intent 호출
    }

    //프로필 사진 다이얼로그
    public void confirmProfilePhotoDialog() {
        //수정 혹은 삭제 커스텀 다이얼로그를 띄움
        CharSequence info[] = new CharSequence[]{"갤러리에서 가져오기", "카메라에서 가져오기"};

        AlertDialog.Builder builder = new AlertDialog.Builder(EditMyPageActivity.this);
        builder.setTitle("제목");
        builder.setItems(info, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        //갤러리에서 가져오는 경우
                        //사진을 갤러리에서 가져오는 메소드 호출
                        selectGallery();
                        break;
                    case 1:
                        // 카메라에서 가져오는 경우
                        //사진을 카메라에서 가져오는 메소드 호출
                        selectPhoto();
                        break;
                }
                dialog.dismiss();
            }
        });
        builder.show();
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
        MemberDao memberDao = new MemberDao(EditMyPageActivity.this);
        FolderDto folderDto = memberDao.readMemberDataPathInfo(loginEmailKey);

        //경로 지정
        String profilePath = folderDto.getProfileDirectoryPath();//파일 경로 지정
        //        String profileTitle = loginEmailKey + ".png"; //파일 이름 지정
        //특수문자가 들어가면 안되므로 문자열의 특수문자 변경
        String tempProfileName = loginEmailKey.replace("@", "_");
        String accountProfileName = tempProfileName.replace(".", "_");
        String profileTitle = accountProfileName + ".png"; //파일 이름 지정
        newBitmapPath = profilePath + "/" + profileTitle;

        File photo = new File(profilePath + "/", profileTitle);

        return photo;
    }

    //호출한 액티비티로부터 결과값을 받아와 그에 따른 이벤트를 수행하는 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //요청 코드가 닉네임 수정인 경우
        if (requestCode == UPDATE_NICKNAME_REQUEST) {
            //이름(별명) 수정을 성공적으로 수행한 경우
            if (resultCode == RESULT_OK) {
                //수정된 이름을 화면에 보여줌
                txtNickName.setText(data.getStringExtra("updateNickName"));
            }
        }

        //요청 코드가 상태 메세지 수정인 경우
        else if (requestCode == UPDATE_PROFILE_MESSAGE_REQUEST) {
            //이름(별명) 수정을 성공적으로 수행한 경우
            if (resultCode == RESULT_OK) {
                //수정된 이름을 화면에 보여줌
                txtProfileMessage.setText(data.getStringExtra("updateProfileMessage"));
            }
        }

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
        intent.putExtra("noFaceDetection",true); //인물 사진도 고정 비율
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
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
        imgProfilePhoto.setImageBitmap(newBitmap);//이미지 뷰에 비트맵 넣기

        //이미지 버튼 둥글게 만들기
        imgProfilePhoto.setBackground(new ShapeDrawable(new OvalShape()));
        if (Build.VERSION.SDK_INT >= 21) {
            imgProfilePhoto.setClipToOutline(true);
        }

        //변경된 값을 shared preference에 반영하는 쿼리 생성
        MemberDao memberDao = new MemberDao(this);
        MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey);
        memberDto.setMemberProfilePhotoPath(newBitmapPath);
        memberDao.updateOneMemberInfo(memberDto); //업데이트 쿼리를 수행

        //변경된 값을 firebase에 반영하는 쿼리 생성
        memberDao.updateOneMemberInfoToFirebase(memberDto);
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
