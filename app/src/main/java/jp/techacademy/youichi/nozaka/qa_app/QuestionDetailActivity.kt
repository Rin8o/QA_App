package jp.techacademy.youichi.nozaka.qa_app

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import com.google.android.gms.tasks.OnCompleteListener

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.list_question_detail.*

import java.util.HashMap

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private var fav = false // お気に入りチェック用変数

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
        }

        override fun onCancelled(databaseError: DatabaseError) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        val dataBaseReference = FirebaseDatabase.getInstance().reference

        // ログインしていれば、お気に入りボタン表示
        val user = FirebaseAuth.getInstance().currentUser // ログイン済みのユーザーを取得する
        if(user != null) {
            findViewById<View>(R.id.add_Fav_button).visibility = View.VISIBLE
            val favRef = dataBaseReference.child(FavPATH).child(user!!.uid).child(mQuestion.questionUid)

            /* お気に入りチェック */
            favRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.value as Map<*, *>?
                    if (data != null) { // favorite内にデータがあった場合
                        fav = true
                        add_Fav_button.isPressed = true // ボタンを押下状態に変更
                    } else {
                        fav = false
                        add_Fav_button.isPressed = false // ボタンを通常状態に変更
                    }
                }

                override fun onCancelled(firebaseError: DatabaseError) {}
            }
            )
        }
        else{}

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        add_Fav_button.setOnClickListener {
            val favRef = dataBaseReference.child(FavPATH).child(user!!.uid).child(mQuestion.questionUid)
            val data = HashMap<String, String>()

            if(fav)
                favRef.removeValue()
            else {
                data["genre"] = mQuestion.genre.toString()
                favRef.setValue(data)
            }
        }
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)
    }


    override fun onResume() {
        super.onResume()

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        val user = FirebaseAuth.getInstance().currentUser // ログイン済みのユーザーを取得する

        if(user != null) {
            val favRef = dataBaseReference.child(FavPATH).child(user!!.uid).child(mQuestion.questionUid)

            /* お気に入りチェック */
            favRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.value as Map<*, *>?
                    if (data != null) { // favorite内にデータがあった場合
                        fav = true
                        add_Fav_button.isPressed = true // ボタンを押下状態に変更
                    } else {
                        fav = false
                        add_Fav_button.isPressed = false // ボタンを通常状態に変更
                    }
                }

                override fun onCancelled(firebaseError: DatabaseError) {}
            }
            )
        }


    }

}