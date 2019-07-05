package com.remindfeedbackproject.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.remindfeedbackproject.Dto.MemberDto;
import com.remindfeedbackproject.Etc.NetworkStatus;
import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Adapter.FriendListAdapter;
import com.remindfeedbackproject.Dao.FriendDao;
import com.remindfeedbackproject.Dto.FriendDto;
import com.remindfeedbackproject.Dialog.TitleDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FriendListActivity extends AppCompatActivity {
    //intent의 값을 저장할 문자열
    String loginEmailKey; //로그인을 한 사용자의 이메일 intent data를 저장할 변수
    static final int FRIEND_SEARCH_REQUEST = 130;  // The request code

    //사용자 입력이 있을 변수 선언
    //Recycler View 관련 객체 변수 선언
    ArrayList<FriendDto> friendArrList; //friend recyclerview를 담을 빈 데이터 리스트 변수
    RecyclerView friendRecyclerView; //friend recyclerview 객체 변수
    FriendListAdapter friendListAdapter;//recycler view와 연결할 Adapter 객체
    RecyclerView.LayoutManager layoutManager; //recyclerview 레이아웃매니저 객체 변수

    FloatingActionButton fbtnCreate; //추가 버튼 객체

    //Activity를 최초 화면에 불러올 때 실행하는 메소드
    //여기서 사용할 변수들을 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendlist_activity);

        //intent 값 저장
        loginEmailKey = getIntent().getStringExtra("loginEmailValue");

        //사용자 입력이 있을 객체를 초기화
        //friend Recycler View 관련 객체 초기화
        friendArrList = new ArrayList<FriendDto>(); //친구 목록을 담을 빈 데이터 리스트 생성
        friendRecyclerView = (RecyclerView) findViewById(R.id.friendlist_recyclerview_friend); //recyclerview 생성
        friendRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);//recycler view 아이템을 linear 형태로 보여줌
        friendRecyclerView.setLayoutManager(layoutManager);//layout manager 설정

        //인터넷 연결 상태 확인
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
        if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
            FirebaseDatabase friendDatabase = FirebaseDatabase.getInstance();
            DatabaseReference friendReference = friendDatabase.getReference("friendInfo");

            //회원 고유 키 생성
            final String tempUserKey = loginEmailKey.replace("@", "_");
            String accountMemberKey = "MemberKey_" + tempUserKey.replace(".", "_");

            // firebase database에서 정보를 읽어옴
            friendReference.child(accountMemberKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    friendArrList.clear();
                    for (DataSnapshot fileSnapshot : dataSnapshot.getChildren()) {
                        //친구 정보가 있는지 확인하고 있을 경우에만 저장
                        if(fileSnapshot.exists() == true) {
                            FriendDto friendDto = fileSnapshot.getValue(FriendDto.class);

                            //firebase에서 회원 정보 읽어와서 친구가 탈퇴했는지 안했는지 확인
                            readMemberInfoToFirebase(friendDto);

                            friendArrList.add(friendDto);
                        }
                    }
                    //firebase에서 가져온 값으로 shared preference에 친구 정보 업데이트
                    FriendDao friendDao = new FriendDao(FriendListActivity.this);
                    //shared preference에 친구 정보가 있는지 확인하고 업데이트
                    for (int i = 0; i < friendArrList.size(); i++) {
                        //shared preference에 저장된 값이 없는 경우
                        if(friendDao.getFriendListValue(loginEmailKey).equals("NONE")|friendDao.getFriendListValue(loginEmailKey).equals("")){
                            //shared preference에 친구 정보 추가
                            friendDao.insertOneFriendInfo(loginEmailKey, friendArrList.get(i));
                        }
                        //shared preference에 저장된 값이  있는 경우
                        else{
                            //친구 정보 업데이트
                            friendDao.updateOneFriendInfo(loginEmailKey, friendArrList.get(i).getFriendEmail(), friendArrList.get(i));
                        }
                    }
                    friendListAdapter = new FriendListAdapter(friendArrList);
                    friendRecyclerView.setAdapter(friendListAdapter);

                    //Recycler view의 아이템을 길게 누르는 경우
                    friendListAdapter.setItemLongClick(new FriendListAdapter.ItemLongClick() {
                        @Override
                        public void onLongClick(View view, final int position) {
                            //수정 혹은 삭제 커스텀 다이얼로그를 띄움
                            CharSequence info[] = new CharSequence[]{"이름 변경", "삭제"};

                            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                            builder.setTitle("친구 정보 수정");
                            builder.setItems(info, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                                    switch (which) {
                                        case 0:
                                            //인터넷 연결 상태 확인
                                            //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
                                            if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
                                                // 이름 변경
                                                Toast.makeText(getApplicationContext(), "이름 변경", Toast.LENGTH_SHORT).show();
                                                //게시글 수정 다이얼로그 메소드 호출
                                                //다이얼로그 객체 생성
                                                TitleDialog updateDialog = new TitleDialog(FriendListActivity.this, "update",
                                                        friendArrList.get(position).getFriendNickName());
                                                updateDialog.setContentView(R.layout.title_dialog); //다이얼로그 레이아웃 지정

                                                //다이얼로그를 보여주기 전 리스너 등록
                                                updateDialog.setTitleDialogListener(new TitleDialog.TitleDialogListener() {
                                                    //생성을 클릭했을 때 제목과 피드백 제공자의 이름을 가져옴
                                                    @Override
                                                    public void onClicked(String state, String fileTitle) {
                                                        updateItemInRecyclerView(position, "update", fileTitle);
                                                    }
                                                });

                                                //다이얼로그 크기 조정
                                                DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();//디바이스 화면 크기 구함
                                                int width = displayMetrics.widthPixels;//디바이스 화면 너비
                                                int height = displayMetrics.heightPixels;//디바이스 화면 높이

                                                WindowManager.LayoutParams wm = updateDialog.getWindow().getAttributes();
                                                wm.width = width;//화면의 가로 넓이
                                                wm.height = height / 3;//화면의 세로 넓이
                                                updateDialog.getWindow().setAttributes(wm);
                                                //게시판 생성 다이얼로그 호출
                                                updateDialog.show();
                                            }
                                            //인터넷 연결이 되어있지 않은 경우
                                            else{
                                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(FriendListActivity.this);
                                                builder.setTitle("인터넷 연결 확인");
                                                builder.setMessage("인터넷이 연결되어있지 않아 친구 이름을 수정할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

                                                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                    }
                                                });
                                                builder.show();
                                            }
                                            break;
                                        case 1:
                                            //인터넷 연결 상태 확인
                                            //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
                                            if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
                                                // 삭제
                                                Toast.makeText(getApplicationContext(), "삭제", Toast.LENGTH_SHORT).show();
                                                //삭제 확인하는 alert dialog 불러옴
                                                delConfirmAlertDialog(position);
                                            }
                                            //인터넷 연결이 되어있지 않은 경우
                                            else{
                                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(FriendListActivity.this);
                                                builder.setTitle("인터넷 연결 확인");
                                                builder.setMessage("인터넷이 연결되어있지 않아 친구를 삭제할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

                                                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                    }
                                                });
                                                builder.show();
                                            }
                                            break;
                                    }
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        }
                    });
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("TAG: ", "Failed to read value", databaseError.toException());

                }
            });
        }
        //인터넷 연결 되어있지 않은 경우
        else {
            //로컬에서 친구 목록 불러와서 저장
            FriendDao friendDao = new FriendDao(this);
            //친구 목록이 있는지 없는지 확인(NONE을 반환하면 없는 것)
            if (!friendDao.getFriendListValue(loginEmailKey).equals("NONE")) {
                //친구목록이 있는 경우 친구목록 저장
                friendArrList = friendDao.readAllFriendInfo(loginEmailKey);
            }
            friendListAdapter = new FriendListAdapter(friendArrList);
            friendRecyclerView.setAdapter(friendListAdapter);

            //Recycler view의 아이템을 길게 누르는 경우
            friendListAdapter.setItemLongClick(new FriendListAdapter.ItemLongClick() {
                @Override
                public void onLongClick(View view, final int position) {
                    //수정 혹은 삭제 커스텀 다이얼로그를 띄움
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(FriendListActivity.this);
                    builder.setTitle("인터넷 연결 확인");
                    builder.setMessage("인터넷이 연결되어있지 않아 친구를 삭제할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
            });
        }

        //화면 버튼 관련 객체 초기화
        fbtnCreate = (FloatingActionButton) findViewById(R.id.friendlist_fbtn_create);
    }

    //View를 화면에 완전히 불러온 후 실행하는 메소드
    //사용자가 화면과 상호작용을 할 때 일어나는 일에 대한 작업을 함
    //친구 목록 화면이므로 친구 추가/수정/삭제 등을 할 수 있음
    @Override
    public void onResume() {
        super.onResume();

        //친구 추가 버튼을 누르는 경우
        //친구 추가 버튼 이벤트를 받는 리스너
        fbtnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //친구 추가를 위해 Edit Activity 호출
                launchFriendListToEditScreen();
            }
        });
    }

    //recycler view 아이템 삭제 확인하는 dialog 띄우는 메소드
    private void delConfirmAlertDialog(final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FriendListActivity.this);
        alertDialogBuilder.setTitle("친구 삭제");
        alertDialogBuilder.setMessage("정말 친구를 삭제하시겠습니까?");

        //아니오를 선택한 경우 alert dialog 끔
        alertDialogBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        //예를 선택한 경우 삭제 메소드 호출
        alertDialogBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItemInRecyclerView(position);
            }
        });
        alertDialogBuilder.show();//alert dialog 보여줌
    }

    //recycler view에 친구를 추가하는 메소드
    private void createItemInRecyclerView(String newFriendEmail, String newFriendName, String newFriendProfilePhotoPath,
                                          String newFriendProfileMessage) {
        //친구 추가 메소드 호출
        FriendDao friendDao = new FriendDao(FriendListActivity.this);
        FriendDto friendDto = new FriendDto(); //친구 정보를 저장할 객체 선언 및 초기화

        //회원 친구 고유 키 생성
        String tempFriendKey = newFriendEmail.replace("@", "_");
        String accountFriendKey = "FriendKey_" + tempFriendKey.replace(".", "_");

        friendDto.setFriendListItem(accountFriendKey, newFriendEmail, newFriendName, newFriendProfilePhotoPath,
                newFriendProfileMessage, "false");

        //shared preference에 친구 정보를 저장
        friendDao.insertOneFriendInfo(loginEmailKey, friendDto);
        //firebase에 친구 정보를 저장
        friendDao.insertOneFriendInfoToFirebase(loginEmailKey, friendDto);

        //친구 관계 업데이트
        updateFriendRelationship(friendDto.getFriendEmail());

        //아이템이 추가되었다는 메세지 화면에 띄우기
        Toast.makeText(this, "친구를 추가하였습니다.", Toast.LENGTH_LONG).show();
    }

    //recycler view에 아이템을 수정하는 메소드
    private void updateItemInRecyclerView(int position, String state, String newFriendName) {
        if (state.equals("update")) {
            //firebase에서 친구목록 불러와서 저장
            FriendDao friendDao = new FriendDao(this);
            FriendDto friendDto = friendArrList.get(position);
            friendDto.setFriendNickName(newFriendName);

            //shared preference에 저장된 친구 데이터 변경
            friendDao.updateOneFriendInfo(loginEmailKey, friendArrList.get(position).getFriendEmail(), friendDto);

            //firebase에 저장된 친구 데이터 번경
            friendDao.updateOneFriendInfoToFirebase(loginEmailKey, friendDto);

            //아이템이 추가되었다는 메세지 화면에 띄우기
            Toast.makeText(this, "친구 이름을 수정하였습니다.", Toast.LENGTH_LONG).show();
        }
    }

    //recycler view에 아이템을 삭제하는 메소드
    private void deleteItemInRecyclerView(int position) {
        //FriendDao에서 데이터 삭제 메소드 호출
        FriendDao friendDao = new FriendDao(this);

        //shared preference에 저장된 친구 데이터 변경
        friendDao.deleteOneFriendInfo(loginEmailKey, friendArrList.get(position).getFriendEmail());

        //firebase에 저장된 친구 데이터 변경
        friendDao.deleteOneFriendInfoToFirebase(loginEmailKey, friendArrList.get(position));
    }

    //친구 목록 화면에서 마이 페이지 화면을 호출하는 메소드
    private void launchFriendListToMyPageScreen(int position) {
        Intent intent = new Intent(FriendListActivity.this, MyPageActivity.class); //새로운 인텐트 객체 생성
        intent.putExtra("loginEmailValue", loginEmailKey); //이메일 값을 intent로 전달

        intent.putExtra("friendEmailValue", friendArrList.get(position).getFriendEmail()); //친구 이메일 값을 intent로 전달
        startActivity(intent); //액티비티 종료하지 않음
    }

    //친구목록 화면에서 친구 추가 화면을 호출하는 메소드
    private void launchFriendListToEditScreen() {
        Intent intent = new Intent(FriendListActivity.this, EditActivity.class);
        intent.putExtra("editState", "friendSearch"); //친구 추가를 위한 호출
        intent.putExtra("loginEmailValue", loginEmailKey); //이메일 값 전달

        startActivityForResult(intent, FRIEND_SEARCH_REQUEST); //intent 호출
    }

    //호출한 액티비티로부터 결과값을 받아와 그에 따른 이벤트를 수행하는 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == FRIEND_SEARCH_REQUEST) {
            //친구 찾기를 성공적으로 수행한 경우
            if (resultCode == RESULT_OK) {
                String[] friendData = data.getStringArrayExtra("intentData");
                createItemInRecyclerView(friendData[0], friendData[1], friendData[2], friendData[3]);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] realtime DB 데이터 검색하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void readMemberInfoToFirebase(final FriendDto friendDto) {
        FirebaseDatabase.getInstance().getReference("memberInfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    MemberDto memberDto = postSnapshot.getValue(MemberDto.class);
                    //만일 회원 정보와 친구 정보가 같은 경우(내 친구인 경우)
                    if(memberDto.getMemberEmail().equals(friendDto.getFriendEmail())){
                        if(memberDto.getMemberNickName().equals("anonymous")) {
                            //만일 친구가 회원 탈퇴를 한 경우(친구의 이름, 상태 메세지 정보가 변경됨)
                            friendDto.setFriendRelationship("false"); //친구 관계를 false로 변경
                            friendDto.setFriendProfileMessage("탈퇴한 회원입니다.");
                            FriendDao friendDao = new FriendDao(FriendListActivity.this);
                            friendDao.updateOneFriendInfo(loginEmailKey, friendDto.getFriendEmail(), friendDto);

                            //firebase의 내 친구 정보도 업데이트
                            friendDao.updateOneFriendInfoToFirebase(loginEmailKey, friendDto);
                        }
                        //회원 탈퇴를 하지 않은 경우
                        else {
                            //친구 관계 업데이트
                            updateFriendRelationship(friendDto.getFriendEmail());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("getFirebaseDatabase", "[Register]loadPostionCanceled", databaseError.toException());
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 친구의 친구 정보를 읽어서 친구 관계를 변경하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void updateFriendRelationship(final String friendEmailKey) {
        FirebaseDatabase friendFriendFirebase = FirebaseDatabase.getInstance();
        DatabaseReference friendFriendReference = friendFriendFirebase.getReference("friendInfo");

        //회원 고유 키 생성
        final String tempFriendKey = friendEmailKey.replace("@", "_");
        String accountFriendKey = "MemberKey_" + tempFriendKey.replace(".", "_");

        // firebase database에서 친구의 친구 정보를 읽어옴
        friendFriendReference.child(accountFriendKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot fileSnapshot : dataSnapshot.getChildren()) {
                    //firebase에 회원 정보가 있는지 확인해서 있는 경우에만 담음
                    if(fileSnapshot.exists() == true) {
                        FriendDto friendDto = fileSnapshot.getValue(FriendDto.class);

                        //친구 목록에 있는 이메일 값이 로그인 한 이메일 값과 똑같은 경우
                        if(loginEmailKey.equals(friendDto.getFriendEmail())){
                            //TRUE 값을 shared preference와 firebase에 저장
                            FriendDao friendDao = new FriendDao(FriendListActivity.this);
                            FriendDto updateFriendDto = friendDao.readOneFriendInfo(loginEmailKey, friendEmailKey);
                            updateFriendDto.setFriendRelationship("true");

                            //shared preference에 업데이트
                            friendDao.updateOneFriendInfo(loginEmailKey, friendEmailKey, updateFriendDto);
                            //firebase에 업데이트
                            friendDao.updateOneFriendInfoToFirebase(loginEmailKey, updateFriendDto);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG: ", "Failed to read value", databaseError.toException());
            }
        });
    }
}