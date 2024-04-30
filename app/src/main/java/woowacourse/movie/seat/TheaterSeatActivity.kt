package woowacourse.movie.seat

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import woowacourse.movie.R
import woowacourse.movie.model.theater.Seat
import woowacourse.movie.model.theater.Theater
import woowacourse.movie.purchaseConfirmation.PurchaseConfirmationActivity

@SuppressLint("DiscouragedApi")
class TheaterSeatActivity : AppCompatActivity(), TheaterSeatContract.View {
    private lateinit var presenter: TheaterSeatPresenter
    private val totalPrice: TextView by lazy { findViewById(R.id.total_price) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seat)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initializePresenter()
        setupSeats()

        findViewById<Button>(R.id.confirm_button).setOnClickListener {
            showConfirmationDialog()
        }
    }

    private fun initializePresenter() {
        val intent = intent
        val ticketNum = intent.getIntExtra("ticketNum", 0)
        presenter = TheaterSeatPresenter(this, ticketNum)
    }

    private fun setupSeats() {
        val tableLayout = findViewById<TableLayout>(R.id.seatTable)
        tableLayout.children.filterIsInstance<TableRow>()
            .forEach { row ->
                row.children.filterIsInstance<Button>()
                    .forEach { button ->
                        button.setOnClickListener {
                            presenter.toggleSeatSelection(button.text.toString())
                        }
                    }
            }
    }

    override fun updateSeatDisplay(seat: Seat) {
        val buttonId = resources.getIdentifier("${seat.row}${seat.number}", "id", packageName)
        val button = findViewById<Button>(buttonId)
        val color = if (seat.chosen) Color.RED else Color.WHITE
        button.setBackgroundColor(color)
    }

    override fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("예매 확인")
        builder.setMessage("정말 예매하시겠습니까?")
        builder.setPositiveButton("예매 완료") { _, _ ->
            val theater = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("Theater", Theater::class.java)
            } else {
                TODO("VERSION.SDK_INT < TIRAMISU")
            }
            val ticketPrice = findViewById<TextView>(R.id.total_price).text
            if (theater != null) {
                val intent = Intent(this, PurchaseConfirmationActivity::class.java).apply {
                    putExtra("ticketPrice", ticketPrice.toString())
                    putExtra("seatNumber", presenter.getSelectedSeatNumbers())
                    putExtra("Theater", theater)
                }
                navigateToNextPage(intent)
            } else {
                Toast.makeText(this, "Theater data is not available.", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }


    override fun setSeatBackground(seatId: String, color: String) {
        val buttonId = resources.getIdentifier(seatId, "id", packageName)
        val button = findViewById<Button>(buttonId)
        val colorInt = Color.parseColor(color)
        button.setBackgroundColor(colorInt)
    }

    override fun updateTotalPrice(price: Int) {
        totalPrice.text = "Total Price: $price"
    }

    override fun navigateToNextPage(intent: Intent) {
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}
