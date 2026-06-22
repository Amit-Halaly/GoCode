package com.example.gocode.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gocode.R
import com.example.gocode.models.AvatarItem
import com.example.gocode.repositories.AvatarRepository
import com.google.android.material.bottomnavigation.BottomNavigationView

class MarketFragment : Fragment() {
    private val avatars by lazy { AvatarRepository.load(requireContext()) }
    private var adapter: MarketAvatarAdapter? = null
    private lateinit var coinsText: TextView
    private lateinit var stateText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_market, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        coinsText = view.findViewById(R.id.marketCoinsText)
        stateText = view.findViewById(R.id.marketStateText)

        val recycler = view.findViewById<RecyclerView>(R.id.marketAvatarRecycler)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.setHasFixedSize(true)

        view.post {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(
                R.id.bottom_navigation
            )
            recycler.setPadding(
                recycler.paddingLeft,
                recycler.paddingTop,
                recycler.paddingRight,
                recycler.paddingBottom + bottomNav.height
            )
        }

        loadMarket()
    }

    override fun onResume() {
        super.onResume()
        if (::coinsText.isInitialized) loadMarket()
    }

    private fun loadMarket() {
        stateText.text = "Loading market..."
        MarketRepository.loadProfile(avatars) { profile ->
            if (!isAdded) return@loadProfile
            if (profile == null) {
                stateText.text = "Sign in to unlock avatars."
                coinsText.text = "0"
                return@loadProfile
            }

            renderProfile(profile)
            stateText.text = "Spend coins on avatars, then equip your favorite look."
        }
    }

    private fun renderProfile(profile: MarketProfile) {
        coinsText.text = profile.coins.toString()
        val currentAdapter = adapter
        if (currentAdapter == null) {
            adapter = MarketAvatarAdapter(
                items = avatars,
                profile = profile,
                onBuy = ::buyAvatar,
                onEquip = ::equipAvatar
            )
            view?.findViewById<RecyclerView>(R.id.marketAvatarRecycler)?.adapter = adapter
        } else {
            currentAdapter.updateProfile(profile)
        }
    }

    private fun buyAvatar(avatar: AvatarItem) {
        stateText.text = "Opening ${avatar.displayName}..."
        MarketRepository.buyAvatar(avatars, avatar) { result ->
            if (!isAdded) return@buyAvatar
            when (result) {
                is MarketActionResult.Success -> {
                    renderProfile(result.profile)
                    stateText.text = "${avatar.displayName} unlocked and equipped."
                    showPurchasePopup(avatar)
                }

                is MarketActionResult.Failure -> {
                    stateText.text = result.message
                }
            }
        }
    }

    private fun equipAvatar(avatar: AvatarItem) {
        stateText.text = "Equipping ${avatar.displayName}..."
        MarketRepository.equipAvatar(avatars, avatar.id) { result ->
            if (!isAdded) return@equipAvatar
            when (result) {
                is MarketActionResult.Success -> {
                    renderProfile(result.profile)
                    stateText.text = "${avatar.displayName} equipped."
                }

                is MarketActionResult.Failure -> {
                    stateText.text = result.message
                }
            }
        }
    }

    private fun showPurchasePopup(avatar: AvatarItem) {
        val imageRes = AvatarRepository.resolveDrawableResId(requireContext(), avatar.drawableName)
        if (imageRes == 0) return
        AvatarPurchaseBottomSheet.newInstance(
            imageRes = imageRes,
            avatarName = avatar.displayName,
            rarity = avatar.rarity,
            price = avatar.price
        ).show(parentFragmentManager, "avatar_purchase")
    }
}
