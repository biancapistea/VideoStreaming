package com.example.videostreaming

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.VideoView
import androidx.fragment.app.Fragment
import com.example.videostreaming.media.VideoLogic
import fm.liveswitch.Future
import fm.liveswitch.IAction1
import fm.liveswitch.Promise


class StartingFragment : Fragment() {
    private var appInstance: VideoLogic? = null
    private var joinButton: Button? = null
    private var leaveButton: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        appInstance = VideoLogic.getInstance(requireActivity().baseContext)
        val startingView: View = inflater.inflate(R.layout.fragment_starting, container, false)
        joinButton = startingView.findViewById<View>(R.id.joinButton) as Button
        leaveButton = startingView.findViewById<View>(R.id.leaveButton) as Button
        joinButton?.isClickable = true
        leaveButton?.isClickable = false
        return startingView
    }

    override fun onStart() {
        super.onStart()
        setUpButtons()
    }

    private fun setStatusText(text: String?) {
        if (view != null) {
            requireView().post {
                val statusText =
                    requireView().findViewById<View>(R.id.appStatusText) as TextView
                statusText.text = text
            }
        }
    }

    private fun setButtonJoinClickable(clickable: Boolean) {
        if (view != null) {
            requireView().post { joinButton?.isClickable = clickable }
        }
    }

    private fun setButtonStopClickable(clickable: Boolean) {
        if (view != null) {
            requireView().post { leaveButton?.isClickable = clickable }
        }
    }

    private fun setUpButtons() {
        joinButton!!.setOnClickListener {
            start().then {
                setButtonJoinClickable(false)
                setButtonStopClickable(true)
            }
        }
        leaveButton!!.setOnClickListener {
            stop().then {
                setButtonJoinClickable(true)
                setButtonStopClickable(false)
            }
        }
    }

    private fun getVideoContainer(): RelativeLayout {
        return requireActivity().findViewById<View>(R.id.videoContainer) as RelativeLayout
    }

    private fun start(): Future<Any?> {
        val promise = Promise<Any?>()
        appInstance?.startLocalMedia(requireActivity(), getVideoContainer())?.then {
            val videoView = getVideoContainer().findViewById<VideoView>(R.id.videoView)
            videoView.visibility = View.GONE
            appInstance?.joinAsync()?.then {
                val message = String.format(
                    "Client %s has successfully joined channel %s.",
                    appInstance!!.getClient().id,
                    appInstance!!.getChannel().id
                )
                setStatusText(message)
                promise.resolve(null)
            }?.fail(IAction1 { ex ->
                setStatusText("Unable to join channel.")
                promise.reject(ex)
            })
        }?.fail(IAction1 { ex ->
            setStatusText("Unable to start local media.")
            promise.reject(ex)
        })
        return promise
    }

    private fun stop(): Future<Any?> {
        val promise = Promise<Any?>()
        if (appInstance?.getClient() != null) {
            appInstance?.leaveAsync()?.then {
                appInstance?.stopLocalMedia()?.then {
                    setStatusText("Application successfully stopped local media.")
                    promise.resolve(null)
                }?.fail(IAction1 { ex ->
                    setStatusText("Unable to stop local media.")
                    promise.reject(ex)
                })
            }?.fail(IAction1 { ex ->
                setStatusText(
                    String.format(
                        "Unable to leave channel %s.",
                        appInstance?.getChannel()?.id
                    )
                )
                promise.reject(ex)
            })
        } else {
            promise.resolve(null)
        }
        return promise
    }

    companion object {
        fun newInstance(): StartingFragment {
            val fragment = StartingFragment()
            fragment.setArguments(Bundle())
            return fragment
        }
    }
}
