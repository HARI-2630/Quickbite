// QuickBite Order Tracking Canvas Engine

export class DeliveryTracker {
  constructor(canvasElement, onStatusChange, onDelivered) {
    this.canvas = canvasElement;
    this.ctx = canvasElement.getContext('2d');
    this.onStatusChange = onStatusChange;
    this.onDelivered = onDelivered;
    
    this.progress = 0; // 0 to 1
    this.speed = 0.001; // Animation speed (default takes about ~15-20 seconds to complete)
    this.isPaused = false;
    this.animationFrameId = null;

    // Define simulation map geometry (scaled relative to canvas dimensions)
    this.width = this.canvas.width;
    this.height = this.canvas.height;

    // Fixed coordinates for entities
    this.restaurant = { x: 80, y: 100, label: 'Burger Craft & Co.' };
    this.home = { x: 380, y: 320, label: 'Your Home' };

    // Path segments (street routing coordinate nodes)
    this.pathPoints = [
      { x: 80, y: 100 },
      { x: 200, y: 100 },
      { x: 200, y: 220 },
      { x: 320, y: 220 },
      { x: 320, y: 320 },
      { x: 380, y: 320 }
    ];

    // Background decoration shapes
    this.parks = [
      { x: 230, y: 40, w: 120, h: 100, color: '#e8f5e9', darkColor: '#1b3a24' },
      { x: 40, y: 240, w: 100, h: 120, color: '#e8f5e9', darkColor: '#1b3a24' }
    ];

    this.river = [
      { x: 0, y: 180 },
      { x: 140, y: 190 },
      { x: 280, y: 170 },
      { x: 480, y: 190 }
    ];

    // Active status mapping based on progress
    this.statusMilestones = [
      { maxProgress: 0.15, code: 'received', title: 'Order Received', desc: 'The restaurant has confirmed your order.' },
      { maxProgress: 0.40, code: 'preparing', title: 'Preparing Meal', desc: 'Our chef is preparing your gourmet meal.' },
      { maxProgress: 0.75, code: 'picked_up', title: 'Out for Delivery', desc: 'Your rider has picked up the order and is on the way.' },
      { maxProgress: 0.95, code: 'arriving', title: 'Arriving Soon', desc: 'Your rider is just a block away!' },
      { maxProgress: 1.0, code: 'delivered', title: 'Delivered', desc: 'Enjoy your meal!' }
    ];
    
    this.currentStatusIndex = -1;

    // Bind resize handler
    this.resizeCanvas();
    window.addEventListener('resize', () => this.resizeCanvas());
  }

  resizeCanvas() {
    // Keep canvas drawing buffer matched to container client size
    const rect = this.canvas.getBoundingClientRect();
    this.canvas.width = rect.width * window.devicePixelRatio;
    this.canvas.height = rect.height * window.devicePixelRatio;
    this.ctx.scale(window.devicePixelRatio, window.devicePixelRatio);
    
    this.width = rect.width;
    this.height = rect.height;
    
    // Scale coordinates proportionally to screen size
    this.restaurant = { x: this.width * 0.15, y: this.height * 0.2, label: 'Restaurant' };
    this.home = { x: this.width * 0.82, y: this.height * 0.8, label: 'Your Home' };
    
    this.pathPoints = [
      { x: this.restaurant.x, y: this.restaurant.y },
      { x: this.width * 0.45, y: this.restaurant.y },
      { x: this.width * 0.45, y: this.height * 0.5 },
      { x: this.width * 0.72, y: this.height * 0.5 },
      { x: this.width * 0.72, y: this.home.y },
      { x: this.home.x, y: this.home.y }
    ];
    
    this.parks = [
      { x: this.width * 0.55, y: this.height * 0.1, w: this.width * 0.3, h: this.height * 0.25, color: '#e8f5e9', darkColor: '#14251a' },
      { x: this.width * 0.08, y: this.height * 0.6, w: this.width * 0.25, h: this.height * 0.3, color: '#e8f5e9', darkColor: '#14251a' }
    ];
    
    this.river = [
      { x: 0, y: this.height * 0.42 },
      { x: this.width * 0.3, y: this.height * 0.45 },
      { x: this.width * 0.6, y: this.height * 0.38 },
      { x: this.width, y: this.height * 0.44 }
    ];
  }

  start() {
    this.progress = 0;
    this.isPaused = false;
    this.currentStatusIndex = -1;
    this.loop();
  }

  stop() {
    this.isPaused = true;
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
    }
  }

  fastForward() {
    this.speed = 0.015; // Set speed to super fast
  }

  resetSpeed() {
    this.speed = 0.001; // Return to standard speed
  }

  // Linear interpolation between two coordinates
  lerp(start, end, amt) {
    return (1 - amt) * start + amt * end;
  }

  // Calculate coordinates of rider along the multi-segmented path based on progress [0..1]
  getRiderPosition() {
    const segmentsCount = this.pathPoints.length - 1;
    const progressPerSegment = 1 / segmentsCount;
    
    const segmentIndex = Math.min(
      Math.floor(this.progress / progressPerSegment),
      segmentsCount - 1
    );
    
    const segmentProgress = (this.progress - (segmentIndex * progressPerSegment)) / progressPerSegment;
    
    const startPoint = this.pathPoints[segmentIndex];
    const endPoint = this.pathPoints[segmentIndex + 1];
    
    return {
      x: this.lerp(startPoint.x, endPoint.x, segmentProgress),
      y: this.lerp(startPoint.y, endPoint.y, segmentProgress)
    };
  }

  checkStatusMilestones() {
    let activeMilestoneIndex = 0;
    for (let i = 0; i < this.statusMilestones.length; i++) {
      if (this.progress >= this.statusMilestones[i].maxProgress - 0.01) {
        activeMilestoneIndex = i;
      }
    }

    if (activeMilestoneIndex !== this.currentStatusIndex) {
      this.currentStatusIndex = activeMilestoneIndex;
      const milestone = this.statusMilestones[activeMilestoneIndex];
      this.onStatusChange(milestone.code, milestone.title, milestone.desc);
    }
  }

  drawMap() {
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    const ctx = this.ctx;
    
    // Clear canvas
    ctx.fillStyle = isDark ? '#12141c' : '#f8f9fa';
    ctx.fillRect(0, 0, this.width, this.height);

    // Draw Parks
    this.parks.forEach(park => {
      ctx.fillStyle = isDark ? park.darkColor : park.color;
      ctx.beginPath();
      ctx.roundRect(park.x, park.y, park.w, park.h, 12);
      ctx.fill();
    });

    // Draw River
    ctx.strokeStyle = isDark ? '#1a2c42' : '#e3f2fd';
    ctx.lineWidth = 28;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.beginPath();
    ctx.moveTo(this.river[0].x, this.river[0].y);
    for (let i = 1; i < this.river.length; i++) {
      ctx.lineTo(this.river[i].x, this.river[i].y);
    }
    ctx.stroke();

    // Draw Streets/Grid (subtle background grid lines representing city streets)
    ctx.strokeStyle = isDark ? '#1b1e2a' : '#eceff1';
    ctx.lineWidth = 10;
    ctx.lineCap = 'square';
    
    // Grid horizontal roads
    for (let y = 50; y < this.height; y += 80) {
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(this.width, y);
      ctx.stroke();
    }
    // Grid vertical roads
    for (let x = 50; x < this.width; x += 80) {
      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x, this.height);
      ctx.stroke();
    }

    // Draw Active Delivery Path
    ctx.strokeStyle = isDark ? '#2e2554' : '#e0dbf8';
    ctx.lineWidth = 14;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.beginPath();
    ctx.moveTo(this.pathPoints[0].x, this.pathPoints[0].y);
    for (let i = 1; i < this.pathPoints.length; i++) {
      ctx.lineTo(this.pathPoints[i].x, this.pathPoints[i].y);
    }
    ctx.stroke();

    // Highlight traversed path
    const riderPos = this.getRiderPosition();
    ctx.strokeStyle = isDark ? '#7047eb' : '#a29bfe';
    ctx.lineWidth = 8;
    ctx.beginPath();
    ctx.moveTo(this.pathPoints[0].x, this.pathPoints[0].y);
    
    // Find how many full segments we passed
    const segmentsCount = this.pathPoints.length - 1;
    const progressPerSegment = 1 / segmentsCount;
    const activeSegmentIdx = Math.min(Math.floor(this.progress / progressPerSegment), segmentsCount - 1);
    
    for (let i = 1; i <= activeSegmentIdx; i++) {
      ctx.lineTo(this.pathPoints[i].x, this.pathPoints[i].y);
    }
    ctx.lineTo(riderPos.x, riderPos.y);
    ctx.stroke();

    // Draw Pins
    // Restaurant Marker
    this.drawPin(this.restaurant.x, this.restaurant.y, '🍔', isDark);
    
    // Home Marker
    this.drawPin(this.home.x, this.home.y, '🏠', isDark);

    // Draw labels
    ctx.fillStyle = isDark ? '#f8f9fa' : '#1e272e';
    ctx.font = 'bold 10px sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText('Burger Craft', this.restaurant.x, this.restaurant.y - 28);
    ctx.fillText('Your Home', this.home.x, this.home.y - 28);

    // Draw Delivery Rider
    this.drawRider(riderPos.x, riderPos.y, isDark);
  }

  drawPin(x, y, emoji, isDark) {
    const ctx = this.ctx;
    
    // Shadow
    ctx.fillStyle = 'rgba(0, 0, 0, 0.15)';
    ctx.beginPath();
    ctx.ellipse(x, y + 2, 8, 4, 0, 0, Math.PI * 2);
    ctx.fill();

    // Pin body
    ctx.fillStyle = isDark ? '#1b1e2a' : '#ffffff';
    ctx.strokeStyle = '#ff5e3a';
    ctx.lineWidth = 3;
    ctx.beginPath();
    ctx.arc(x, y - 12, 14, 0, Math.PI * 2);
    ctx.fill();
    ctx.stroke();

    // Pin tip
    ctx.fillStyle = '#ff5e3a';
    ctx.beginPath();
    ctx.moveTo(x - 5, y - 3);
    ctx.lineTo(x, y + 1);
    ctx.lineTo(x + 5, y - 3);
    ctx.fill();

    // Emoji icon
    ctx.font = '14px serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(emoji, x, y - 12);
  }

  drawRider(x, y, isDark) {
    const ctx = this.ctx;

    // Glowing aura
    const gradient = ctx.createRadialGradient(x, y, 2, x, y, 18);
    gradient.addColorStop(0, 'rgba(112, 71, 235, 0.4)');
    gradient.addColorStop(1, 'rgba(112, 71, 235, 0)');
    ctx.fillStyle = gradient;
    ctx.beginPath();
    ctx.arc(x, y, 18, 0, Math.PI * 2);
    ctx.fill();

    // Shadow
    ctx.fillStyle = 'rgba(0, 0, 0, 0.2)';
    ctx.beginPath();
    ctx.ellipse(x, y + 4, 10, 4, 0, 0, Math.PI * 2);
    ctx.fill();

    // Scooter badge
    ctx.fillStyle = '#7047eb';
    ctx.strokeStyle = '#ffffff';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.arc(x, y, 12, 0, Math.PI * 2);
    ctx.fill();
    ctx.stroke();

    // Scooter icon (emoji)
    ctx.font = '11px serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('🛵', x, y);
  }

  loop() {
    if (this.isPaused) return;

    // Advance progress
    this.progress += this.speed;
    
    if (this.progress >= 1.0) {
      this.progress = 1.0;
      this.drawMap();
      this.checkStatusMilestones();
      this.stop();
      this.onDelivered();
      return;
    }

    this.drawMap();
    this.checkStatusMilestones();

    this.animationFrameId = requestAnimationFrame(() => this.loop());
  }
}
