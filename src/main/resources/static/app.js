class CacheAlgorithm {
    constructor(capacity) {
        this.capacity = capacity;
        this.cache = [];
        this.accessCount = new Map();
    }
}

class LRUCache extends CacheAlgorithm {
    get(key) {
        const index = this.cache.findIndex(item => item === key);
        if (index !== -1) {
            this.cache.splice(index, 1);
            this.cache.push(key);
            return key;
        }
        return null;
    }

    put(key) {
        const index = this.cache.findIndex(item => item === key);
        if (index !== -1) {
            this.cache.splice(index, 1);
            this.cache.push(key);
            return;
        }

        this.cache.push(key);

        if (this.cache.length > this.capacity) {
            this.cache.shift();
        }
    }

    getSnapshot() {
        return [...this.cache];
    }
}

class LFUCache extends CacheAlgorithm {
    constructor(capacity) {
        super(capacity);
        this.frequencies = new Map();
    }

    get(key) {
        const index = this.cache.findIndex(item => item === key);
        if (index !== -1) {
            const currentFreq = this.frequencies.get(key) || 0;
            this.frequencies.set(key, currentFreq + 1);
            return key;
        }
        return null;
    }

    put(key) {
        if (this.cache.includes(key)) {
            const currentFreq = this.frequencies.get(key) || 0;
            this.frequencies.set(key, currentFreq + 1);
            return;
        }

        if (this.cache.length >= this.capacity) {
            let minFreq = Infinity;
            let lfuKey = null;
            let lfuIndex = -1;

            for (let i = 0; i < this.cache.length; i++) {
                const item = this.cache[i];
                const freq = this.frequencies.get(item) || 0;
                if (freq < minFreq) {
                    minFreq = freq;
                    lfuKey = item;
                    lfuIndex = i;
                }
            }

            if (lfuKey) {
                this.cache.splice(lfuIndex, 1);
                this.frequencies.delete(lfuKey);
            }
        }

        this.cache.push(key);
        this.frequencies.set(key, 1);
    }

    getSnapshot() {
        return [...this.cache];
    }
}

class FIFOCache extends CacheAlgorithm {
    get(key) {
        return this.cache.includes(key) ? key : null;
    }

    put(key) {
        if (this.cache.includes(key)) return;

        this.cache.push(key);
        if (this.cache.length > this.capacity) {
            this.cache.shift();
        }
    }
    getSnapshot() {
        return [...this.cache];
    }
}

let edgeCache1 = new LRUCache(2);
let edgeCache2 = new LRUCache(2);
let replicaCache1 = new LRUCache(2);
let replicaCache2 = new LRUCache(2);
let replicaCache3 = new LRUCache(2);

let edge1RequestCount = 0;
let edge2RequestCount = 0;
let replica1RequestCount = 0;
let replica2RequestCount = 0;
let replica3RequestCount = 0;

let totalRequests = 0;
let cacheHits = 0;
let cacheMisses = 0;
let isRequestInProgress = false;

let edge1CurrentAlg = 'LRU';
let edge2CurrentAlg = 'LRU';
let replica1CurrentAlg = 'LRU';
let replica2CurrentAlg = 'LRU';
let replica3CurrentAlg = 'LRU';

const originResources = {
    'img1': {
        id: 'img1',
        name: 'Bali Sunset',
        type: 'image',
        size: '1.3MB',
        description: 'Beautiful sunset over Bali beaches',
    },
    'img2': {
        id: 'img2',
        name: 'Mountain Bike',
        type: 'image',
        size: '64KB',
        description: 'Mountain bike photo',
    },
    'img3': {
        id: 'img3',
        name: 'Dolphins',
        type: 'image',
        size: '80KB',
        description: 'Pod of dolphins',
    },
    'img4': {
        id: 'img4',
        name: 'Bird',
        type: 'image',
        size: '164KB',
        description: 'Bird on a tree',
    },
    'img5': {
        id: 'img5',
        name: 'Tigers',
        type: 'image',
        size: '691KB',
        description: 'Two tigers hugging',
    },
    'img6': {
        id: 'img6',
        name: 'City',
        type: 'image',
        size: '670KB',
        description: 'The heart of Sydney',
    }
};

function updateCacheAlgorithm(serverType, algorithm) {
    const capacity = 2;

    function createCache(alg) {
        switch (alg) {
            case 'LFU': return new LFUCache(capacity);
            case 'FIFO': return new FIFOCache(capacity);
            default: return new LRUCache(capacity);
        }
    }

    if (serverType === 'edge') {
        if (algorithm !== edge1CurrentAlg) {
            edgeCache1 = createCache(algorithm);
            edge1CurrentAlg = algorithm;
            renderCache([], 'edge1Cache');
            updateCacheDisplay('edgeServer1', algorithm);
        }
        if (algorithm !== edge2CurrentAlg) {
            edgeCache2 = createCache(algorithm);
            edge2CurrentAlg = algorithm;
            renderCache([], 'edge2Cache');
            updateCacheDisplay('edgeServer2', algorithm);
        }
    } else if (serverType === 'replica') {
        if (algorithm !== replica1CurrentAlg) {
            replicaCache1 = createCache(algorithm);
            replica1CurrentAlg = algorithm;
            renderCache([], 'replica1Cache');
            updateCacheDisplay('replicaServer1', algorithm);
        }
        if (algorithm !== replica2CurrentAlg) {
            replicaCache2 = createCache(algorithm);
            replica2CurrentAlg = algorithm;
            renderCache([], 'replica2Cache');
            updateCacheDisplay('replicaServer2', algorithm);
        }
        if (algorithm !== replica3CurrentAlg) {
            replicaCache3 = createCache(algorithm);
            replica3CurrentAlg = algorithm;
            renderCache([], 'replica3Cache');
            updateCacheDisplay('replicaServer3', algorithm);
        }
    }
}

function updateCacheDisplay(serverId, algorithm) {
    const titleElement = document.querySelector(`#${serverId} .cache-title`);
    if (titleElement) {
        titleElement.textContent = `Cache (${algorithm}):`;
    }
}

function renderCache(cacheItems, containerId, hitItem = null) {
    const container = document.getElementById(containerId);
    container.innerHTML = '';

    cacheItems.forEach(item => {
        const div = document.createElement('div');
        div.className = 'cache-item';
        div.textContent = item;

        if (item === hitItem) {
            div.classList.add('hit');
        }
        container.appendChild(div);
    });

    if (cacheItems.length === 0) {
        container.innerHTML = '<span style="color: #999; font-style: italic;">Empty</span>';
    }
}

function createArrow(fromId, toId, isReturn = false) {
    const fromElement = document.getElementById(fromId);
    const toElement = document.getElementById(toId);
    const diagram = document.getElementById('networkDiagram');

    if (!fromElement || !toElement || !diagram) return null;

    const existingArrows = diagram.querySelectorAll('.arrow');
    existingArrows.forEach(arrow => arrow.remove());

    const fromRect = fromElement.getBoundingClientRect();
    const toRect = toElement.getBoundingClientRect();
    const diagramRect = diagram.getBoundingClientRect();

    const fromCenterX = fromRect.left + fromRect.width / 2 - diagramRect.left;
    const fromCenterY = fromRect.top + fromRect.height / 2 - diagramRect.top;
    const toCenterX = toRect.left + toRect.width / 2 - diagramRect.left;
    const toCenterY = toRect.top + toRect.height / 2 - diagramRect.top;

    const deltaX = toCenterX - fromCenterX;
    const deltaY = toCenterY - fromCenterY;
    const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

    if (distance < 50) return null;

    const dirX = deltaX / distance;
    const dirY = deltaY / distance;

    const startX = fromCenterX + dirX * 80;
    const startY = fromCenterY + dirY * 80;

    const endX = toCenterX - dirX * 80;
    const endY = toCenterY - dirY * 80;

    const finalDeltaX = endX - startX;
    const finalDeltaY = endY - startY;
    const finalLength = Math.sqrt(finalDeltaX * finalDeltaX + finalDeltaY * finalDeltaY);
    const angle = Math.atan2(finalDeltaY, finalDeltaX) * 180 / Math.PI;

    const arrow = document.createElement('div');
    arrow.className = isReturn ? 'arrow return' : 'arrow';
    arrow.style.position = 'absolute';
    arrow.style.left = startX + 'px';
    arrow.style.top = (startY - 2) + 'px';
    arrow.style.width = '0px';
    arrow.style.height = '4px';
    arrow.style.transform = `rotate(${angle}deg)`;
    arrow.style.transformOrigin = '0 50%';
    arrow.style.zIndex = '25';

    diagram.appendChild(arrow);

    requestAnimationFrame(() => {
        arrow.style.transition = 'width 1s ease-out';
        arrow.style.width = finalLength + 'px';
    });

    const removeTimeout = setTimeout(() => {
        if (arrow && arrow.parentNode) {
            arrow.remove();
        }
    }, 1500);

    arrow.removeTimeout = removeTimeout;
    return arrow;
}

function clearAllArrows() {
    const diagram = document.getElementById('networkDiagram');
    const arrows = diagram.querySelectorAll('.arrow');
    arrows.forEach(arrow => {
        if (arrow.removeTimeout) {
            clearTimeout(arrow.removeTimeout);
        }
        arrow.remove();
    });
}

function activateServer(serverId, isHit = false) {
    document.querySelectorAll('.server-node').forEach(node => {
        node.classList.remove('active', 'hit');
    });

    const server = document.getElementById(serverId);
    if (server) {
        if (isHit) {
            server.classList.add('hit');
        } else {
            server.classList.add('active');
        }
    }
}

function addTraceEntry(message, isHit = null) {
    const traceLog = document.getElementById('traceLog');
    const entry = document.createElement('div');
    entry.className = 'trace-entry';
    entry.textContent = `${new Date().toLocaleTimeString()} - ${message}`;

    if (isHit === true) {
        entry.classList.add('hit');
    } else if (isHit === false) {
        entry.classList.add('miss');
    }

    traceLog.appendChild(entry);
    traceLog.scrollTop = traceLog.scrollHeight;
}

function updateStats() {
    document.getElementById('totalRequests').textContent = totalRequests;
    document.getElementById('cacheHits').textContent = cacheHits;
    document.getElementById('cacheMisses').textContent = cacheMisses;

    const hitRatio = totalRequests > 0 ? Math.round((cacheHits / totalRequests) * 100) : 0;
    document.getElementById('hitRatio').textContent = hitRatio + '%';

    document.getElementById('edge1Requests').textContent = edge1RequestCount;
    document.getElementById('edge2Requests').textContent = edge2RequestCount;
    document.getElementById('replica1Requests').textContent = replica1RequestCount;
    document.getElementById('replica2Requests').textContent = replica2RequestCount;
    document.getElementById('replica3Requests').textContent = replica3RequestCount;
}

document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('resourceSelect').addEventListener('change', function(e) {
        const urlBox = document.getElementById('customUrlInput');
        if (e.target.value === 'custom') {
            urlBox.style.display = 'block';
        } else {
            urlBox.style.display = 'none';
        }
    });
});

function displayResource(resourceId) {
    const box = document.getElementById('resourceContent');

    if (resourceId.startsWith('custom_')) {
        const url = document.getElementById('customUrlInput').value.trim();
        box.classList.remove('loaded');

        if (url) {
            box.innerHTML = `
                <div style="text-align:center;max-width:100%">
                    <h3>Custom Resource</h3>
                    <p><strong>URL:</strong> ${url}</p>
                    <p><strong>Type:</strong> Custom</p>
                    <p style="margin-bottom:10px;">User-specified resource</p>
                    <img src="${url}" alt="Custom Resource" style="max-width:100%;max-height:250px;border-radius:10px;box-shadow:0 5px 15px rgba(0,0,0,.2);" 
                         onerror="this.outerHTML='<p style=&quot;color:#c00;padding:20px;&quot;>Unable to load image from: ${url}</p>'" />
                </div>`;
        } else {
            box.innerHTML = `<p style="color:#c00">No URL has been provided.</p>`;
        }

        requestAnimationFrame(() => box.classList.add('loaded'));
        return;
    }

    if (resourceId === 'custom') {
        const url = document.getElementById('customUrlInput').value.trim();
        box.innerHTML = url
            ? `<img src="${url}" alt="custom resource"
              style="max-width:100%;max-height:250px;border-radius:10px;
                     box-shadow:0 5px 15px rgba(0,0,0,.25);" />`
            : `<p style="color:#c00">No URL has been provided.</p>`;
        requestAnimationFrame(() => box.classList.add('loaded'));
        return;
    }

    box.classList.remove('loaded');

    const resource = originResources[resourceId];
    const fileMap  = { img1:'bali.jpg', img2:'bike.jpg', img3:'dolphins.jpg', img4:'bird.jpg', img5:'tigers.jpg', img6:'city.jpeg'};
    const path     = id => `/img/${fileMap[id]}`;

    box.innerHTML = `
        <div style="text-align:center;max-width:100%">
          <h3>${resource.name}</h3>
          <p><strong>ID:</strong> ${resource.id}</p>
          <p><strong>Type:</strong> ${resource.type}</p>
          <p><strong>Size:</strong> ${resource.size}</p>
          <p style="margin-bottom:10px;">${resource.description}</p>
          <img src="${path(resourceId)}" alt="${resource.name}"
               style="max-width:200%;max-height:250px;border-radius:10px;
                      box-shadow:0 5px 15px rgba(0,0,0,.2);" />
        </div>`;

    requestAnimationFrame(() => box.classList.add('loaded'));
}

function clearCache() {
    clearAllArrows();

    edgeCache1 = edge1CurrentAlg === 'LFU' ? new LFUCache(2) :
        edge1CurrentAlg === 'FIFO' ? new FIFOCache(2) : new LRUCache(2);
    edgeCache2 = edge2CurrentAlg === 'LFU' ? new LFUCache(2) :
        edge2CurrentAlg === 'FIFO' ? new FIFOCache(2) : new LRUCache(2);

    replicaCache1 = replica1CurrentAlg === 'LFU' ? new LFUCache(2) :
        replica1CurrentAlg === 'FIFO' ? new FIFOCache(2) : new LRUCache(2);
    replicaCache2 = replica2CurrentAlg === 'LFU' ? new LFUCache(2) :
        replica2CurrentAlg === 'FIFO' ? new FIFOCache(2) : new LRUCache(2);
    replicaCache3 = replica3CurrentAlg === 'LFU' ? new LFUCache(2) :
        replica3CurrentAlg === 'FIFO' ? new FIFOCache(2) : new LRUCache(2);

    renderCache([], 'edge1Cache');
    renderCache([], 'edge2Cache');
    renderCache([], 'replica1Cache');
    renderCache([], 'replica2Cache');
    renderCache([], 'replica3Cache');

    addTraceEntry('All caches cleared');
    document.querySelectorAll('.server-node').forEach(n =>
        n.classList.remove('active', 'hit')
    );
}

function selectReplicaServer(resourceId = null) {
    const servers = [
        { id: 'replicaServer1', cache: replicaCache1, requests: replica1RequestCount, index: 0 },
        { id: 'replicaServer2', cache: replicaCache2, requests: replica2RequestCount, index: 1 },
        { id: 'replicaServer3', cache: replicaCache3, requests: replica3RequestCount, index: 2 }
    ];

    if (resourceId) {
        for (const server of servers) {
            if (server.cache.cache.includes(resourceId)) {
                console.log(`Cache-aware routing: Found ${resourceId} in ${server.id}`);
                return {
                    serverId: server.id,
                    serverObject: server.cache,
                    serverIndex: server.index
                };
            }
        }
    }

    let minIndex = 0;
    for (let i = 1; i < servers.length; i++) {
        if (servers[i].requests < servers[minIndex].requests) {
            minIndex = i;
        }
    }

    return {
        serverId: servers[minIndex].id,
        serverObject: servers[minIndex].cache,
        serverIndex: minIndex
    };
}

async function simulateRealisticRequest() {
    if (isRequestInProgress) return;

    isRequestInProgress = true;
    document.getElementById('requestBtn').disabled = true;
    clearAllArrows();

    let resourceId = document.getElementById('resourceSelect').value;

    if (resourceId === 'custom') {
        const customUrl = document.getElementById('customUrlInput').value.trim();
        if (!customUrl) {
            alert('Please enter a custom URL!');
            isRequestInProgress = false;
            document.getElementById('requestBtn').disabled = false;
            return;
        }
    }

    const edgeAlgorithm = document.getElementById('edgeAlgorithm').value;
    const replicaAlgorithm = document.getElementById('replicaAlgorithm').value;

    updateCacheAlgorithm('edge', edgeAlgorithm);
    updateCacheAlgorithm('replica', replicaAlgorithm);

    totalRequests++;
    let resourceFound = false;
    let foundAtLevel = null;
    let foundAtServer = null;
    let requestPath = [];

    addTraceEntry(`Client requesting resource: ${resourceId}`);

    activateServer('client');
    await new Promise(res => setTimeout(res, 800));

    const selectedEdge = selectEdgeServer(resourceId);
    createArrow('client', selectedEdge.serverId);
    await new Promise(res => setTimeout(res, 1200));

    if (selectedEdge.serverIndex === 0) {
        edge1RequestCount++;
    } else {
        edge2RequestCount++;
    }

    const edgeHit = selectedEdge.serverObject.get(resourceId);
    requestPath.push({
        serverId: selectedEdge.serverId,
        serverObject: selectedEdge.serverObject,
        cacheElementId: selectedEdge.serverId === 'edgeServer1' ? 'edge1Cache' : 'edge2Cache'
    });

    if (edgeHit) {
        cacheHits++;
        resourceFound = true;
        foundAtLevel = 'edge';
        foundAtServer = selectedEdge.serverId;
        activateServer(selectedEdge.serverId, true);
        addTraceEntry(`CACHE HIT at ${selectedEdge.serverId} for ${resourceId}`, true);
        renderCache(selectedEdge.serverObject.getSnapshot(), selectedEdge.serverId === 'edgeServer1' ? 'edge1Cache' : 'edge2Cache', resourceId);
    } else {
        cacheMisses++;
        activateServer(selectedEdge.serverId, false);
        addTraceEntry(`CACHE MISS at ${selectedEdge.serverId} for ${resourceId}`, false);
        renderCache(selectedEdge.serverObject.getSnapshot(),
            selectedEdge.serverId === 'edgeServer1' ? 'edge1Cache' : 'edge2Cache');

        await new Promise(res => setTimeout(res, 800));
        const selectedReplica = selectReplicaServer(resourceId);
        createArrow(selectedEdge.serverId, selectedReplica.serverId);
        await new Promise(res => setTimeout(res, 1200));

        if (selectedReplica.serverIndex === 0) {
            replica1RequestCount++;
        } else if (selectedReplica.serverIndex === 1) {
            replica2RequestCount++;
        } else {
            replica3RequestCount++;
        }

        const replicaHit = selectedReplica.serverObject.get(resourceId);
        requestPath.push({
            serverId: selectedReplica.serverId,
            serverObject: selectedReplica.serverObject,
            cacheElementId: `replica${selectedReplica.serverIndex + 1}Cache`
        });

        if (replicaHit) {
            cacheHits++;
            resourceFound = true;
            foundAtLevel = 'replica';
            foundAtServer = selectedReplica.serverId;
            activateServer(selectedReplica.serverId, true);
            addTraceEntry(`CACHE HIT at ${selectedReplica.serverId} for ${resourceId}`, true);
            renderCache(selectedReplica.serverObject.getSnapshot(),
                `replica${selectedReplica.serverIndex + 1}Cache`,
                resourceId);
        } else {
            cacheMisses++;
            activateServer(selectedReplica.serverId, false);
            addTraceEntry(`CACHE MISS at ${selectedReplica.serverId} for ${resourceId}`, false);
            renderCache(selectedReplica.serverObject.getSnapshot(),
                `replica${selectedReplica.serverIndex + 1}Cache`);

            await new Promise(res => setTimeout(res, 800));
            createArrow(selectedReplica.serverId, 'originServer');
            await new Promise(res => setTimeout(res, 1200));

            activateServer('originServer', true);
            addTraceEntry(`Fetching ${resourceId} from Origin Server`, true);
            foundAtLevel = 'origin';
            foundAtServer = 'originServer';
            resourceFound = true;
        }
    }

    await new Promise(res => setTimeout(res, 800));

    if (foundAtLevel === 'origin') {
        const returnPath = [...requestPath].reverse();

        const replicaServer = returnPath[0];
        replicaServer.serverObject.put(resourceId);
        createArrow('originServer', replicaServer.serverId, true);
        await new Promise(res => setTimeout(res, 1200));
        renderCache(replicaServer.serverObject.getSnapshot(), replicaServer.cacheElementId);

        const edgeServer = returnPath[1];
        edgeServer.serverObject.put(resourceId);
        createArrow(replicaServer.serverId, edgeServer.serverId, true);
        await new Promise(res => setTimeout(res, 1200));
        renderCache(edgeServer.serverObject.getSnapshot(), edgeServer.cacheElementId);

        createArrow(edgeServer.serverId, 'client', true);
        await new Promise(res => setTimeout(res, 1200));

        addTraceEntry(`Resource ${resourceId} cached only on request path during return`);
    } else if (foundAtLevel === 'replica') {
        const edgeServer = requestPath[0];
        edgeServer.serverObject.put(resourceId);

        createArrow(foundAtServer, edgeServer.serverId, true);
        await new Promise(res => setTimeout(res, 1200));
        renderCache(edgeServer.serverObject.getSnapshot(), edgeServer.cacheElementId);

        createArrow(edgeServer.serverId, 'client', true);
        await new Promise(res => setTimeout(res, 1200));

        addTraceEntry(`Resource ${resourceId} cached in edge server during return`);
    } else if (foundAtLevel === 'edge') {
        createArrow(foundAtServer, 'client', true);
        await new Promise(res => setTimeout(res, 1200));
    }

    activateServer('client', true);
    displayResource(resourceId);
    addTraceEntry(`Resource ${resourceId} delivered to client successfully`);
    updateStats();

    await new Promise(res => setTimeout(res, 1000));
    document.querySelectorAll('.server-node').forEach(node => {
        node.classList.remove('active', 'hit');
    });

    isRequestInProgress = false;
    document.getElementById('requestBtn').disabled = false;
}

class EnhancedLRUCache extends CacheAlgorithm {
    constructor(capacity) {
        super(capacity);
        this.accessOrder = [];
    }

    get(key) {
        const index = this.cache.indexOf(key);
        if (index !== -1) {
            this.cache.splice(index, 1);
            this.cache.push(key);
            return key;
        }
        return null;
    }

    put(key) {
        const index = this.cache.indexOf(key);
        if (index !== -1) {
            this.cache.splice(index, 1);
            this.cache.push(key);
            return;
        }

        this.cache.push(key);

        if (this.cache.length > this.capacity) {
            const evicted = this.cache.shift();
            console.log(`LRU evicted: ${evicted}`);
        }
    }

    getSnapshot() {
        return [...this.cache];
    }
}

class EnhancedLFUCache extends CacheAlgorithm {
    constructor(capacity) {
        super(capacity);
        this.frequencies = new Map();
        this.minFrequency = 1;
    }

    get(key) {
        if (this.cache.includes(key)) {
            const oldFreq = this.frequencies.get(key) || 0;
            this.frequencies.set(key, oldFreq + 1);
            return key;
        }
        return null;
    }

    put(key) {
        if (this.cache.includes(key)) {
            const oldFreq = this.frequencies.get(key) || 0;
            this.frequencies.set(key, oldFreq + 1);
            return;
        }

        if (this.cache.length >= this.capacity) {
            let minFreq = Infinity;
            let lfuKey = null;

            for (const item of this.cache) {
                const freq = this.frequencies.get(item) || 0;
                if (freq < minFreq) {
                    minFreq = freq;
                    lfuKey = item;
                }
            }

            if (lfuKey) {
                const index = this.cache.indexOf(lfuKey);
                this.cache.splice(index, 1);
                this.frequencies.delete(lfuKey);
                console.log(`LFU evicted: ${lfuKey} (freq: ${minFreq})`);
            }
        }

        this.cache.push(key);
        this.frequencies.set(key, 1);
    }

    getSnapshot() {
        return [...this.cache];
    }
}

function selectEdgeServer(resourceId = null) {
    const servers = [
        { id: 'edgeServer1', cache: edgeCache1, requests: edge1RequestCount, index: 0 },
        { id: 'edgeServer2', cache: edgeCache2, requests: edge2RequestCount, index: 1 }
    ];

    if (resourceId) {
        for (const server of servers) {
            if (server.cache.cache.includes(resourceId)) {
                console.log(`Cache-aware routing: Found ${resourceId} in ${server.id}`);
                return {
                    serverId: server.id,
                    serverObject: server.cache,
                    serverIndex: server.index
                };
            }
        }
    }

    const selectedIndex = servers[0].requests <= servers[1].requests ? 0 : 1;
    return {
        serverId: servers[selectedIndex].id,
        serverObject: servers[selectedIndex].cache,
        serverIndex: selectedIndex
    };
}

function selectEdgeServerConsistent(resourceId) {
    const hash = resourceId.split('').reduce((a, b) => a + b.charCodeAt(0), 0);
    const selectedIndex = hash % 2;

    return {
        serverId: ['edgeServer1', 'edgeServer2'][selectedIndex],
        serverObject: [edgeCache1, edgeCache2][selectedIndex],
        serverIndex: selectedIndex
    };
}

let roundRobinCounter = 0;

function selectEdgeServerRoundRobin() {
    const selectedIndex = roundRobinCounter % 2;
    roundRobinCounter++;

    return {
        serverId: ['edgeServer1', 'edgeServer2'][selectedIndex],
        serverObject: [edgeCache1, edgeCache2][selectedIndex],
        serverIndex: selectedIndex
    };
}

function simulateRequest() {
    return simulateRealisticRequest();
}