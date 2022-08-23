package com.kotlin_project.bookreview

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.kotlin_project.bookreview.adapter.BookAdapter
import com.kotlin_project.bookreview.adapter.HistoryAdapter
import com.kotlin_project.bookreview.api.BookService
import com.kotlin_project.bookreview.databinding.ActivityMainBinding
import com.kotlin_project.bookreview.model.BestSellerDto
import com.kotlin_project.bookreview.model.History
import com.kotlin_project.bookreview.model.SearchBookDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bookAdapter: BookAdapter
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var bookService: BookService
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBookRecyclerView()
        initHistoryRecyclerView()

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "BookSearchDB"
        ).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://book.interpark.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        bookService = retrofit.create(BookService::class.java)

        getBestSellerBooks()
        bindView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun bindView() {
        binding.searchEditText.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == MotionEvent.ACTION_DOWN) {
                search(binding.searchEditText.text.toString())
                return@setOnKeyListener true
            }

            return@setOnKeyListener false
        }

        binding.searchEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                showSearchHistroy()
            }

            return@setOnTouchListener false
        }
    }

    private fun getBestSellerBooks() {
        bookService.getBestSellerBooks(getString(R.string.api_key))
            .enqueue(object : Callback<BestSellerDto> {
                override fun onResponse(
                    call: Call<BestSellerDto>,
                    response: Response<BestSellerDto>
                ) {
                    if (response.isSuccessful.not()) {
                        Log.e(TAG, "Fail to get best sellers!")
                        return
                    }

                    bookAdapter.submitList(response.body()?.books.orEmpty())
                }

                override fun onFailure(call: Call<BestSellerDto>, t: Throwable) {
                    Log.e(TAG, "Fail to get best sellers!")
                }

            })
    }

    private fun search(keyword: String) {
        bookService.getBooksByName(getString(R.string.api_key), keyword)
            .enqueue(object : Callback<SearchBookDto> {
                override fun onResponse(
                    call: Call<SearchBookDto>,
                    response: Response<SearchBookDto>
                ) {

                    hideKeyboard()
                    hideSearchHistroy()
                    saveSearchKeyword(keyword)

                    if (response.isSuccessful.not()) {
                        Log.e(TAG, "Fail to get books with the keyword!")
                        return
                    }

                    bookAdapter.submitList(response.body()?.books.orEmpty())

                    if (response.body()?.books?.isEmpty() == true) {
                        Toast.makeText(this@MainActivity, "일치하는 도서가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(this@MainActivity, "${response.body()?.books?.size}개 도서를 찾았습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SearchBookDto>, t: Throwable) {
                    hideSearchHistroy()
                    Log.e(TAG, "Fail to get books with the keyword!")
                }

            })
    }

    private fun initHistoryRecyclerView() {
        historyAdapter = HistoryAdapter(historyDeleteClickedListener = {
            deleteSearchKeyword(it)
        })

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter
    }

    private fun initBookRecyclerView() {
        bookAdapter = BookAdapter()

        binding.bookRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bookRecyclerView.adapter = bookAdapter
    }

    private fun saveSearchKeyword(keyword: String) {
        Thread {
            db.historyDao().insertHistory(History(null, keyword))
        }.start()
    }

    private fun deleteSearchKeyword(keyword: String) {
        Thread {
            db.historyDao().delete(keyword)
            showSearchHistroy()
        }.start()
    }

    private fun showSearchHistroy() {
        Thread {
            val keywords = db.historyDao().getAll().reversed()

            runOnUiThread {
                binding.bookRecyclerView.isVisible = false
                binding.historyRecyclerView.isVisible = true
                historyAdapter.submitList(keywords)
            }
        }.start()
    }

    private fun hideSearchHistroy() {
        binding.historyRecyclerView.isVisible = false
        binding.bookRecyclerView.isVisible = true
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    override fun onBackPressed() {
        if (binding.historyRecyclerView.isVisible) {
            binding.historyRecyclerView.isVisible = false
            binding.bookRecyclerView.isVisible = true

            binding.searchEditText.clearFocus()
            binding.searchEditText.setText("")
        }
        else
            super.onBackPressed()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}