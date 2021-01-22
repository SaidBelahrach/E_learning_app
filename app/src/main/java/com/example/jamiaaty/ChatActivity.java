package com.example.jamiaaty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    ImageView senderIV,receiverIV;
    RecyclerView recyclerView;
    EditText sendMessageET;
    ImageButton sendButton;
    TextView nameReceiverrTV;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String currentUser ="";
    String receiverId,receiverName,receiverUrl,senderName,senderUrl;
    DatabaseReference conversationRef,AllUserRef,chatRef;
    String chatKey = "";
    messageChatAdapter chatAdapter;
    List<chatMessageModel> listMessages = new ArrayList<>();
    List<Boolean> isSenderList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sendButton = findViewById(R.id.ib_send_message_chat);
        senderIV = findViewById(R.id.iv_currentUser_chat);
        receiverIV = findViewById(R.id.iv_receiver_chat);
        sendMessageET = findViewById(R.id.answer_disscussion_tv);
        nameReceiverrTV = findViewById(R.id.name_recevier_chat_tv);
        recyclerView = findViewById(R.id.rv_disscussion);
        if(user != null){
            currentUser = user.getUid();
        }
        AllUserRef = database.getReference("All Users");
        chatRef = database.getReference("chat");

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        AllUserRef.child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    All_UserMemeber memeber = snapshot.getValue(All_UserMemeber.class);
                if (memeber != null) {
                    senderName = memeber.getName();
                    senderUrl = memeber.getUrl();
                }
                if(!senderUrl.equals("")){
                    Picasso.get().load(senderUrl).into(senderIV);
                }
                nameReceiverrTV.setText(receiverName);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Bundle extra = getIntent().getExtras();
        if (extra != null){
            receiverName = extra.getString("rName");
            receiverUrl = extra.getString("rUrl");
            receiverId = extra.getString("rId");
            chatKey = extra.getString("chatKey");
        }else {
            Toast.makeText(this, "opps some thing went wrong !", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!receiverUrl.equals("")){
            Picasso.get().load(receiverUrl).into(receiverIV);
        }


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatMessageModel model = new chatMessageModel();


                Calendar ctime = Calendar.getInstance();
                SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm:ss");
                final String savetime = currenttime.format(ctime.getTime());

                final String time =  savetime;
                if(!sendMessageET.getText().toString().equals("")){
                    model.setMessage(sendMessageET.getText().toString().trim());
                    model.setIdReceivevr(receiverId);
                    model.setIdSender(currentUser);
                    model.setTime(time);
                    chatRef.child(chatKey).push().setValue(model);
                }
                sendMessageET.setText("");

            }
        });

        chatRef.child(chatKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    listMessages.clear();
                    isSenderList.clear();
                    for(DataSnapshot item : snapshot.getChildren()){
                        chatMessageModel model = item.getValue(chatMessageModel.class);
                        if(currentUser.equals(model.getIdSender())){
                            isSenderList.add(true);
                        }else {
                            isSenderList.add(false);
                        }
                        listMessages.add(model);
                    }
                    chatAdapter = new messageChatAdapter(getApplicationContext(),listMessages,isSenderList);
                    recyclerView.setAdapter(chatAdapter);
                    if(listMessages.size() != 0 && (listMessages.size()-1>=0)){
                        recyclerView.smoothScrollToPosition(listMessages.size()-1);
                    }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild(chatKey)){
                    chatRef.child(chatKey).setValue(true);
                }else {
                    chatMessageModel model = snapshot.getValue(chatMessageModel.class);
                        AllUserRef.child(currentUser).child("chatKeys").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(!snapshot.hasChild(chatKey)){
                                    AllUserRef.child(currentUser).child("chatKeys").child(chatKey).setValue(true);
                                    AllUserRef.child(receiverId).child("chatKeys").child(chatKey).setValue(true);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}