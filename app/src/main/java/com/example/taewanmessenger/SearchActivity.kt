package com.example.taewanmessenger

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import com.example.taewanmessenger.Utils.FirestoreUtil
import com.example.taewanmessenger.etc.Dialog_in_SearchItemClicked
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_search.*
import org.jetbrains.anko.sdk27.coroutines.onEditorAction
import org.jetbrains.anko.toast


class SearchActivity : AppCompatActivity() {

    private val TAG = "TAGSearchActivity"
    private val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //상단 툴바 설정
        setSupportActionBar(searchToolbar_toolbar_searchActivity)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_left_arrow)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "친구 찾기"

        //검색결과 리사이클러뷰
        searchItems_recyclerview_searchActivity.layoutManager = GridLayoutManager(this, 2)
        searchItems_recyclerview_searchActivity.setHasFixedSize(true)
        searchItems_recyclerview_searchActivity.adapter = adapter

        /**
         * 검색 자동완성 기능
         * **/
        //자동완성 텍스트뷰 캐스팅(따로 무조건 해줘야함.)
        val edit1 = findViewById(R.id.edit) as AutoCompleteTextView

        //검색어를 누를때마다 파이어스토어에 있는 정보를 실시간으로 불러옴
        FirestoreUtil.autoCompleteSearch(this, edit1)

        //글자 입력 후 그냥 엔터를 눌렀을 경우
        edit1.onEditorAction { v, actionId, event ->
            when(actionId){
                EditorInfo.IME_ACTION_SEARCH -> {
                    adapter.clear()
                    adapter.notifyDataSetChanged()
                    if(edit1.text.isEmpty()){
                        toast("검색어를 입력해주세요")
                    }
                    else{
                        circularProgressButton.startAnimation()
                        FirestoreUtil.fetchSearchedUser(this@SearchActivity, adapter, edit1.text.toString(), noSearchedText){
                            circularProgressButton.revertAnimation()
                            edit1.text.clear()
                        }
                    }
                }
                else -> return@onEditorAction
            }
        }
        //검색 버튼을 누를시
        circularProgressButton.setOnClickListener {
            adapter.clear()
            adapter.notifyDataSetChanged()
            if(edit1.text.isEmpty()){
                toast("검색어를 입력해주세요")
            }
            else{
                circularProgressButton.startAnimation()
                FirestoreUtil.fetchSearchedUser(applicationContext, adapter, edit1.text.toString(), noSearchedText){
                    circularProgressButton.revertAnimation()
                    edit1.text.clear()
                }
            }
        }
    }
}
