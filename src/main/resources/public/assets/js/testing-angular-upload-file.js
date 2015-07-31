var myApp = angular.module('myApp', []);

myApp.directive('fileModel', ['$parse', function ($parse) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;
            
            element.bind('change', function(){
                scope.$apply(function(){
                    modelSetter(scope, element[0].files[0]);
                });
            });
        }
    };
}]);

myApp.service('fileUpload', ['$http', function ($http) {
    this.uploadFileToUrl = function(file, uploadUrl){
        var fd = new FormData();
        fd.append('uploadFile', file);
        fd.append('destinationUri', 'TODO');
        $http.post(uploadUrl, fd, {
            transformRequest: angular.identity,
            headers: {
                'Cache-Control': 'max-age=0',
                'Content-Type': undefined
            }
        })
        .success(function(){
            console.log('SUCCESS');
        })
        .error(function(){
            console.log('ERROR');
        });
    }
}]);

myApp.controller('myCtrl', ['$scope', 'fileUpload', function($scope, fileUpload){
    $scope.uploadProgress = 0;

    $scope.uploadFile = function(){
        var file = $scope.myFile;
        console.log('file is ');
        console.dir(file);
        fileUpload.uploadFileToUrl(file, '/oxides/unicore/upload');
    };
}]);

myApp.directive('oxidesProgressBar', [
    function () {
        return {
            link: function ($scope, el, attrs) {
                $scope.$watch(attrs.progressBar, function (newValue) {
                    el.css('width', newValue.toString() + '%');
                });
            }
        };
    }
]);



myApp.controller('uploadCtrl', ['$scope', '$upload', function ($scope, $upload) {
    $scope.model = {};
    $scope.selectedFile = [];
    $scope.uploadProgress = 0;

    $scope.doUploadFile = function () {
        var file = $scope.selectedFile[0];
        console.log('selected file: ');
        console.log(file);

        $scope.upload = $upload.upload({
            url: '/oxides/unicore/upload',
            method: 'POST',
            data: angular.toJson($scope.model),
            file: file
        }).progress(function (evt) {
            $scope.uploadProgress = parseInt(100.0 * evt.loaded / evt.total, 10);
        }).success(function (data) {
            console.log('Upload successful');
        }).error(function (data) {
            console.log('Upload failed');
        });
    };

    $scope.onFileSelect = function ($files) {
        $scope.uploadProgress = 0;
        $scope.selectedFile = $files;
        console.log('onFileSelect');
    };
}]);

myApp.directive('progressBar', [ function () {
    return {
        link: function ($scope, el, attrs) {
            $scope.$watch(attrs.progressBar, function (newValue) {
                el.css('width', newValue.toString() + '%');
            });
        }
    };
}]);
