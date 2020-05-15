'use strict'

const functions = require('firebase-functions');
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendFollowAndLikeNotification = functions.firestore.document('notification/{notification_id}')
.onWrite((data, context)=>
{
    const notification_id = context.params.notification_id;

    console.log("Notification ID : ",notification_id );

    return admin.firestore().collection("notification").doc(notification_id).get().then(queryResult => {

        console.log("Notification to user : ",queryResult.data().toUserId );

        const tokenId = queryResult.data().toDeviceId;
        const notificationTitle = queryResult.data().title;
        const notificationText = queryResult.data().text;

        console.log("Notification to device : ", tokenId);

        const notificationContent = {
            token : tokenId,
            notification:{
                title: notificationTitle,
                body: notificationText
            }
        };

        return admin.messaging().send(notificationContent).then(result => {
            console.log("Notification sent!"); 
         })
        });
        
    });
