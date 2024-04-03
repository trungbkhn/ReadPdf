package com.example.bookapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.bookapp.databinding.ActivityDashBoardUserBinding
import com.example.bookapp.model.ModelCategory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


private lateinit var binding: ActivityDashBoardUserBinding
private lateinit var categoryArrayList: ArrayList<ModelCategory>
private lateinit var viewPagerAdapter: DashBoardUserActivity.ViewPagerAdapter

class DashBoardUserActivity : AppCompatActivity() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashBoardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogoutDb.setOnClickListener {
            MaterialAlertDialogBuilder(this@DashBoardUserActivity)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out of this app?")
                .setPositiveButton("Yes") { dialog, _ ->
                    dialog.dismiss()
                    firebaseAuth.signOut()
                    startActivity(Intent(this@DashBoardUserActivity, MainActivity::class.java))
                    finish()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this@DashBoardUserActivity,ProfileActivity::class.java))
        }

        checkUser()
        setUpWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)


    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            binding.tvUsernameDb.setText("Not log in")
        } else {
            binding.tvGmailDb.setText(firebaseUser.email)
        }
    }



    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context) :
        FragmentPagerAdapter(fm, behavior) {
        private val fragmentList: ArrayList<BookUserFragment> = ArrayList()
        private val fragmentTitleList: ArrayList<String> = ArrayList()
        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        fun addFragment(fragment: BookUserFragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)

        }
    }

    private fun setUpWithViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this@DashBoardUserActivity
        )

        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Category")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                val modeAll = ModelCategory(1, "All", 1, "")
                val modeViewed = ModelCategory(2, "Most Viewed", 1, "")
                val modeDownloaded = ModelCategory(3, "Most Downloaded", 1, "")
                categoryArrayList.add(modeAll)
                categoryArrayList.add(modeViewed)
                categoryArrayList.add(modeDownloaded)
                viewPagerAdapter.addFragment(
                    BookUserFragment.newInstance(
                        "${modeAll.id}",
                        "${modeAll.category}",
                        "${modeAll.uid}"
                    ), modeAll.category
                )

                viewPagerAdapter.addFragment(
                    BookUserFragment.newInstance(
                        "${modeDownloaded.id}",
                        "${modeDownloaded.category}",
                        "${modeDownloaded.uid}"
                    ), modeDownloaded.category
                )

                viewPagerAdapter.addFragment(
                    BookUserFragment.newInstance(
                        "${modeViewed.id}",
                        "${modeViewed.category}",
                        "${modeViewed.uid}"
                    ), modeViewed.category
                )

                // Load data
                for (i in snapshot.children) {
                    val model = i.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                    viewPagerAdapter.addFragment(
                        BookUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ), model.category
                    )
                }

                // Set adapter after adding all fragments
                viewPager.adapter = viewPagerAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


}