var app = angular.module('AngularUIBucketApp', [
    "ngRoute",
    "ngTouch",
    "mobile-angular-ui"
]);

app.config(function($routeProvider, $locationProvider) {
    $routeProvider.when('/', {
        templateUrl: "signIn.html"
    });
});

app.controller('MainController', ['$scope',
    function($scope) {

        // Initialized the user object
        $scope.user = {
            username: "",
            password: ""
        };

        // Sign In auth function
        $scope.signin = function() {
            var email = $scope.user.username;
            var password = $scope.user.password;
            if (email &amp;&amp; password) {
                // Sign In Logic
            }
        }

    }
]);
