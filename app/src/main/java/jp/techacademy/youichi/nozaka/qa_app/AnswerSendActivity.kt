package jp.techacademy.youichi.nozaka.qa_app

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_answer_send.*
import kotlinx.android.synthetic.main.activity_question_detail.*

import java.util.HashMap

class AnswerSendActivity : AppCompatActivity(), View.OnClickListener, DatabaseReference.CompletionListener {

    private lateinit var mQuestion: Question

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer_send)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question


        var favCheck = "" // お気に入りチェック用変数
        val dataBaseReference = FirebaseDatabase.getInstance().reference
        val favRef = dataBaseReference.child(FavPATH).child(mQuestion.uid).child(mQuestion.questionUid)

        /*
        favRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as Map<*, *>?
                favCheck = data!!["fav"] as String
                if(favCheck == "Yes")
                    add_Fav_button.isPressed = true // お気に入り登録済なら、ボタンを押下状態に変更
                else
                {}
                }
            override fun onCancelled(firebaseError: DatabaseError) {}
        }
        )
         */

        // UIの準備
        sendButton.setOnClickListener(this)
    }

    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
        progressBar.visibility = View.GONE

        if (databaseError == null) {
            finish()
        } else {
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show()
        }

    }

    override fun onClick(v: View) {
        // キーボードが出てたら閉じる
        val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        val answerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)

        val data = HashMap<String, String>()

        // UID
        data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid

        // 表示名
        // Preferenceから名前を取る
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY, "")
        data["name"] = name

        // 回答を取得する
        val answer = answerEditText.text.toString()

        if (answer.isEmpty()) {
            // 回答が入力されていない時はエラーを表示するだけ
            Snackbar.make(v, "回答を入力して下さい", Snackbar.LENGTH_LONG).show()
            return
        }
        data["body"] = answer

        progressBar.visibility = View.VISIBLE
        answerRef.push().setValue(data, this)
    }

}