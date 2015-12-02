/**
 * Created by elfiky on 28/07/15.
 */

var MaqoolaNotification = {

  addNotification: function (successCallback, errorCallback) {
    cordova.exec(
      successCallback, // success callback function
      errorCallback, // error callback function
      'MaqoolaNotificationsPlugin', // mapped to our native Java class called "MaqoolaNotificationsPlugin"
      'add_notification', // with this action name
      []
    );
  },

  downloadNotification: function (url, successCallback, errorCallback) {
    cordova.exec(
      successCallback, // success callback function
      errorCallback, // error callback function
      'MaqoolaNotificationsPlugin', // mapped to our native Java class called "MaqoolaNotificationsPlugin"
      'download_file', // with this action name
      [url]
    );
  }

};

module.exports = MaqoolaNotification;
