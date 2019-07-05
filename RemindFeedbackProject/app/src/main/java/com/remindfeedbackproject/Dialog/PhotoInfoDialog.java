package com.remindfeedbackproject.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.remindfeedbackproject.R;

public class PhotoInfoDialog extends Dialog implements View.OnClickListener {
    //atvitiy에 넘겨줄 입력값을 가지는 인터페이스 선언
    public interface PhotoInfoDialogListener {
        //파일의 이름을 넘겨줌
        void onClicked(String dialogState, String fileTitle);
    }
    private PhotoInfoDialogListener photoInfoDialogListener;//인터페이스를 멤버 변수로 선언
    private Context context; //Fragment에서 받아올 Context

    //fragment에서 받아온 item 내용을 저장할 변수 선언
    private String infoTitle;
    private String infoBitmapPath;
    private String infoTime;

    //생성자
    public PhotoInfoDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    //받아올 값이 있을 때의 생성자
    public PhotoInfoDialog(Context context, String infoTitle, String infoBitmapPath, String infoTime) {
        super(context);
        this.context = context;
        this.infoTitle = infoTitle;
        this.infoBitmapPath = infoBitmapPath;
        this.infoTime = infoTime;
    }

    //photoInfoDialogListener 초기화하는 메소드
    public void setPhotoInfoDialogListener(PhotoInfoDialogListener photoInfoDialogListener) {
        this.photoInfoDialogListener = photoInfoDialogListener;
    }

    //Dialog를 최초 화면에 불러올 때 실행하는 메소드
    //여기서 사용할 변수들을 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView txtTitle = (TextView) findViewById(R.id.fragment2_itm_info_txt_title);
        ImageView imgContent = (ImageView) findViewById(R.id.fragment2_itm_info_img_content);
        TextView txtTime = (TextView) findViewById(R.id.fragment2_itm_info_txt_time);

        txtTitle.setText(infoTitle);
        //이미지 파일의 경로에서 이미지를 불러와 적용
        Bitmap imgBitmap = BitmapFactory.decodeFile(infoBitmapPath);
        imgContent.setImageBitmap(imgBitmap);
        txtTime.setText(infoTime);
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}
