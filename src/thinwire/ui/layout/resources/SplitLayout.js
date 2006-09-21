/*
 #LICENSE_HEADER#
 #VERSION_HEADER#
 */
tw_SplitLayout = Class.extend({
    _comp: null,
    _vertical: true,
    _drag: null,
    
    construct: function(id) {
        this._comp = tw_Component.instances[id];
        this._drag = new tw_DragHandler(this._comp._box, this._dragListener.bind(this));
        this._vertical = this._comp.getWidth() < this._comp.getHeight();
        this._comp._box.style.cursor = this._vertical ? "W-resize" : "N-resize";
    },
    
    _dragListener: function(ev) {        
        if (ev.type == 0) {
            this._comp._box.style.backgroundColor = "black";
            this._vertical = this._comp.getWidth() < this._comp.getHeight();
        } else if (ev.type == 1) {
            if (this._vertical) {
                this._comp._box.style.cursor = "W-resize";
                var x = this._comp.getX() + ev.changeInX;
                if (x < 0) x = 0;
                this._comp.setX(x);
            } else {
                this._comp._box.style.cursor = "N-resize";
                var y = this._comp.getY() + ev.changeInY;
                if (y < 0) y = 0;
                this._comp.setY(y);
            }
        } else if (ev.type == 2) {
            this._comp._box.style.backgroundColor = "transparent";
            tw_em.sendViewStateChanged(this._comp._id, "position", this._comp.getX() + "," + this._comp.getY());            
        }
    },
    
    destroy: function() {
        this._drag.destroy();
    }
});

tw_SplitLayout.newInstance = function(id) {
    new tw_SplitLayout(id);
};
