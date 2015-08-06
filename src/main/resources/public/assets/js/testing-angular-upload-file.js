var myApp = angular.module('myApp', [
    'angularFileUpload'
]);

myApp.controller('uploadController', function ($scope, FileUploader) {

    $scope.uploader = new FileUploader({
        url: '/oxides/unicore/upload',
        alias: 'uploadFile',
        queueLimit: 1,
        formData: [{
            destinationUri: 'TODO'
        }],
        removeAfterUpload: true,

        //onBeforeUploadItem: function (item) {
        //    Array.prototype.push.apply(item.formData, [{
        //        destinationUri: 'TODO',
        //    }]);
        //},

        onSuccessItem: function (item, response, status, headers) {
            console.log('SUCCESS');
        },

        onCompleteItem: function (item, response, status, headers) {
            this.clearQueue();
            console.log('STATUS: ' + status);
        },
    });

    //$scope.uploader.FileSelect.prototype.isEmptyAfterSelection = function () {
    //    return false; // true|false
    //};
});

