package com.example.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.content.ContentUris
import kotlinx.android.synthetic.main.activity_main.*
import android.net.Uri
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    private val imageUriList = arrayListOf<Uri>();

    private var selectedNum = 0;

    private var mTimer: Timer? = null

    // タイマー用の時間のための変数
    private var mTimerSec = 0.0

    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
        if (imageUriList.isNotEmpty()) {
            imageView.setImageURI(imageUriList.get(selectedNum))
        }
        next_button.setOnClickListener{
            showNextImage()
        }
        back_button.setOnClickListener{
            showBackImage()
        }

        start_button.setOnClickListener {
            if (mTimer == null){
                // 停止状態→再生
                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mTimerSec += 0.1
                        if (mTimerSec >= 2.0) {
                            showNextImage()
                            mTimerSec = 0.0
                        }
                    }
                }, 100, 100) // 最初に始動させるまで 100ミリ秒、ループの間隔を 100ミリ秒 に設定
                next_button.isClickable = false
                back_button.isClickable = false
                start_button.text = "停止"
            } else {
                // 再生状態→停止
                if (mTimer != null){
                    mTimer!!.cancel()
                    mTimer = null
                }
                mTimerSec = 0.0
                next_button.isClickable = true
                back_button.isClickable = true
                start_button.text = "再生"
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                imageUriList.add(imageUri);
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    private fun showNextImage() {
        selectedNum++
        if (selectedNum >= imageUriList.size) {
            selectedNum = 0
        }
        if (imageUriList.isNotEmpty()) {
            mHandler.post {
                imageView.setImageURI(imageUriList.get(selectedNum))
            }
        }
    }

    private fun showBackImage() {
        selectedNum--
        if (selectedNum < 0) {
            selectedNum = imageUriList.size - 1
        }
        if (imageUriList.isNotEmpty()) {
            mHandler.post {
                imageView.setImageURI(imageUriList.get(selectedNum))
            }
        }
    }
}