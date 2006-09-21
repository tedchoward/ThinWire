/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
// Add support for creating functions that are tighly bound to Objects.
Function.prototype.bind = function (object) {
    var method = this;
    
    return function () {
        method.apply(object, arguments);
    };
}

//Defines the top level Class
function Class() { }
Class.extend = function(def) {
    //This creates a subclass using supported JS techniques for subclassing.
    //We pass a reference to Class, mearly as a marker for checking whether
    //the constructor should run or not.  We don't want the constructor for
    //the super class to run when were simply creating the prototype for the
    //sub class
    var classDef = function() {
        if (arguments[0] !== Class) { this.construct.apply(this, arguments); }
    };
    
    var proto = new this(Class);
    var superClass = this.prototype;
    
    //Override all methods of the parent object with this objects definiton    
    if (superClass === Class.prototype) {
        for (var n in def) {
            proto[n] = def[n];
        }
    } else {
        for (var n in def) {
            var item = def[n];                        
            
            if (item instanceof Function) {
                item = Class.__asMethod__(item, superClass);
            }
            
            proto[n] = item;
        }
    }

    var setters = {};
    
    for (var n in proto) {
        if (n.indexOf("set") == 0 && proto[n] instanceof Function) {            
            setters[n.charAt(3).toLowerCase() + n.substring(4)] = n;
        }
    }
    
    proto.__setters__ = setters;
    proto.$ = superClass;
    classDef.prototype = proto;
    
    //Give this new class the same static extend method    
    classDef.extend = this.extend;        
    return classDef;
};

Class.__asMethod__ = function(func, superClass) {    
    return function() {
        var currentSuperClass = this.$;
        this.$ = superClass;
        var ret = func.apply(this, arguments);        
        this.$ = currentSuperClass;
        return ret;
    };
};

Class.prototype = {    
    construct: function() {}
};

