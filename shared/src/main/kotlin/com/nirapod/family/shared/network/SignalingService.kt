package com.nirapod.family.shared.network

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nirapod.family.shared.models.SignalingMessage
import com.nirapod.family.shared.util.Constants
import org.webrtc.IceCandidate

class SignalingService(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private val ref = db.getReference(Constants.REALTIME_SIGNALING)

    fun sendOffer(sdp: String, senderId: String, targetId: String, streamType: String) {
        val msg = SignalingMessage(
            type = "offer",
            sdp = sdp,
            senderDeviceId = senderId,
            targetDeviceId = targetId,
            streamType = streamType
        )
        ref.child(targetId).push().setValue(msg)
    }

    fun sendAnswer(sdp: String, senderId: String, targetId: String, streamType: String) {
        val msg = SignalingMessage(
            type = "answer",
            sdp = sdp,
            senderDeviceId = senderId,
            targetDeviceId = targetId,
            streamType = streamType
        )
        ref.child(targetId).push().setValue(msg)
    }

    fun sendCandidate(candidate: IceCandidate, senderId: String, targetId: String, streamType: String) {
        val msg = SignalingMessage(
            type = "ice_candidate",
            candidate = candidate.sdp,
            sdpMid = candidate.sdpMid,
            sdpMLineIndex = candidate.sdpMLineIndex,
            senderDeviceId = senderId,
            targetDeviceId = targetId,
            streamType = streamType
        )
        ref.child(targetId).push().setValue(msg)
    }

    fun listenForSignaling(myDeviceId: String, onMessage: (SignalingMessage) -> Unit) {
        val myRef = ref.child(myDeviceId)
        myRef.addChildEventListener(object : com.google.firebase.database.ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val msg = snapshot.getValue(SignalingMessage::class.java)
                if (msg != null) {
                    onMessage(msg)
                    // Remove message once processed
                    snapshot.ref.removeValue()
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
