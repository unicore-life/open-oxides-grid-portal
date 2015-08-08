var myApp = angular.module('myApp', [
    'angularFileUpload',
    'angular-spinkit'
]);


myApp.controller('spinkitCtrl', function ($scope, $http) {
    $scope.spinkitData = 'x';
    $scope.spinkitShow = false;

    $scope.getPageAndShow = function () {
        $scope.spinkitData = '';
        $scope.spinkitShow = true;

        var request = {
            method: 'GET',
            url: '/testing-long-call',
            headers: {'Content-Type': 'application/json'},
            data: ''
        };
        $http(request).then(
            function (response) {
                $scope.spinkitData = response.data;
                $scope.spinkitShow = false;
            },
            function (response) {
                console.warn('Failed: HTTP Status Code = ' + response.status);
                $scope.spinkitData = response.statusText;
                $scope.spinkitShow = false;
            }
        );
    };
});


myApp.controller('uploadController', function ($scope, FileUploader) {
    $scope.destinationUri = 'TODO';

    $scope.uploader = new FileUploader({
        url: '/oxides/unicore/upload',
        alias: 'uploadFile',
        queueLimit: 1,
//        formData: [{
//            destinationUri: $scope.destinationUri
//        }],
        removeAfterUpload: true,

        onBeforeUploadItem: function (item) {
            Array.prototype.push.apply(item.formData, [{
                destinationUri: $scope.destinationUri,
            }]);
        },

        onSuccessItem: function (item, response, status, headers) {
            console.info('SUCCESS');
        },

        onCompleteItem: function (item, response, status, headers) {
            this.clearQueue();
            // Workaround for re-upload:
            $scope.uploader._directives.select[0].element[0].value = '';
            console.log('STATUS: ' + status);
        },
    });

//    $scope.uploader.FileSelect.prototype.isEmptyAfterSelection = function () {
//        return true; // true|false
//    };
});

