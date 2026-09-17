const API_BASE = '/api/cdn';

const state = {
    resources: new Map(),
    totalRequests: 0,
    cacheHits: 0,
    cacheMisses: 0,
    edgeARequests: 0,
    edgeBRequests: 0,
    replicaUsRequests: 0,
    replicaEuRequests: 0,
    requestInProgress: false
};

document.addEventListener('DOMContentLoaded', async () => {
    document.getElementById('requestBtn')
        .addEventListener('click', simulateRequest);

    document.getElementById('clearCacheBtn')
        .addEventListener('click', clearCache);

    await loadResources();
    updateStats();
});

async function loadResources() {
    const select = document.getElementById('resourceSelect');
    const originContainer = document.getElementById('originResources');

    try {
        const response = await fetch(`${API_BASE}/resources`);
        ensureSuccessfulResponse(response);

        const resources = await response.json();

        select.innerHTML = '';
        originContainer.innerHTML = '';
        state.resources.clear();

        resources.forEach(resource => {
            state.resources.set(resource.id, resource);

            const option = document.createElement('option');
            option.value = resource.id;
            option.textContent = `${resource.name} (${resource.id})`;
            select.appendChild(option);

            const item = document.createElement('div');
            item.className = 'cache-item';
            item.textContent = resource.id;
            originContainer.appendChild(item);
        });

        if (resources.length === 0) {
            select.innerHTML = '<option value="">No resources available</option>';
            originContainer.innerHTML = '<span class="cache-empty">No resources</span>';
        }
    } catch (error) {
        select.innerHTML = '<option value="">Unable to load resources</option>';
        originContainer.innerHTML = '<span class="cache-empty">Unavailable</span>';
        addTraceEntry(error.message, false);
    }
}

async function simulateRequest() {
    if (state.requestInProgress) {
        return;
    }

    const resourceId = document.getElementById('resourceSelect').value;

    if (!resourceId) {
        addTraceEntry('Please select a resource.', false);
        return;
    }

    const resource = state.resources.get(resourceId);

    if (!resource) {
        addTraceEntry(`Unknown resource: ${resourceId}`, false);
        return;
    }

    state.requestInProgress = true;
    setButtonsDisabled(true);
    clearAllArrows();
    resetServerStates();

    addTraceEntry(`Client requesting resource: ${resourceId}`);

    activateServer('client');
    await delay(500);

    try {
        const response = await fetch(`${API_BASE}/clientRequest`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                clientId: getClientId(),
                resourceId: resource.id,
                url: resource.path
            })
        });

        ensureSuccessfulResponse(response);

        const result = await response.json();

        state.totalRequests++;

        const requestHit = Array.isArray(result.trace)
            && result.trace.some(hop => hop.hit);

        if (requestHit) {
            state.cacheHits++;
        } else {
            state.cacheMisses++;
        }

        await animateTrace(result.trace, resourceId);

        displayResource(resource, result.resourcePath);

        addTraceEntry(
            `Resource ${resourceId} delivered to client successfully`,
            true
        );

        updateStats();
    } catch (error) {
        addTraceEntry(error.message, false);
    } finally {
        await delay(500);
        resetServerStates();

        state.requestInProgress = false;
        setButtonsDisabled(false);
    }
}

async function animateTrace(trace, resourceId) {
    if (!Array.isArray(trace) || trace.length === 0) {
        addTraceEntry('No request trace was returned.', false);
        return;
    }

    let previousServer = 'client';

    for (const hop of trace) {
        const serverId = hop.serverId;

        updateRequestCounter(serverId);

        createArrow(previousServer, serverId);

        await delay(850);

        activateServer(serverId, hop.hit);

        renderCache(
            Array.isArray(hop.cache) ? hop.cache : [],
            `${serverId}-cache`,
            hop.hit ? resourceId : null
        );

        addTraceEntry(
            `${hop.hit ? 'CACHE HIT' : 'CACHE MISS'} at ${serverId} for ${resourceId}`,
            hop.hit
        );

        previousServer = serverId;

        if (hop.hit) {
            break;
        }

        await delay(500);
    }

    const lastHop = trace[trace.length - 1];

    if (!lastHop.hit) {
        createArrow(previousServer, 'originServer');

        await delay(850);

        activateServer('originServer', true);

        addTraceEntry(
            `Fetching ${resourceId} from Origin Server`,
            true
        );

        previousServer = 'originServer';
    }

    await delay(500);

    createArrow(previousServer, 'client', true);

    await delay(850);

    activateServer('client', true);
}

function updateRequestCounter(serverId) {
    switch (serverId) {
        case 'edge-a':
            state.edgeARequests++;
            break;

        case 'edge-b':
            state.edgeBRequests++;
            break;

        case 'replica-us-east':
            state.replicaUsRequests++;
            break;

        case 'replica-eu-west':
            state.replicaEuRequests++;
            break;
    }
}

async function clearCache() {
    if (state.requestInProgress) {
        return;
    }

    try {
        setButtonsDisabled(true);

        const response = await fetch('/cache/clear', {
            method: 'DELETE'
        });

        ensureSuccessfulResponse(response);

        [
            'edge-a-cache',
            'edge-b-cache',
            'replica-us-east-cache',
            'replica-eu-west-cache'
        ].forEach(id => {
            renderCache([], id);
        });

        clearAllArrows();
        resetServerStates();

        addTraceEntry('All backend caches cleared', true);
    } catch (error) {
        addTraceEntry(error.message, false);
    } finally {
        setButtonsDisabled(false);
    }
}

function renderCache(cacheItems, containerId, hitItem = null) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = '';

    if (!Array.isArray(cacheItems) || cacheItems.length === 0) {
        const empty = document.createElement('span');
        empty.className = 'cache-empty';
        empty.textContent = 'Empty';

        container.appendChild(empty);
        return;
    }

    cacheItems.forEach(item => {
        const element = document.createElement('div');

        element.className = 'cache-item';
        element.textContent = item;

        if (item === hitItem) {
            element.classList.add('hit');
        }

        container.appendChild(element);
    });
}

function displayResource(resource, resourcePath) {
    const container = document.getElementById('resourceContent');

    container.classList.remove('loaded');
    container.innerHTML = '';

    const wrapper = document.createElement('div');
    wrapper.style.textAlign = 'center';
    wrapper.style.maxWidth = '100%';

    const title = document.createElement('h3');
    title.textContent = resource.name;

    const id = document.createElement('p');
    id.innerHTML = `<strong>ID:</strong> ${escapeHtml(resource.id)}`;

    const type = document.createElement('p');
    type.innerHTML = `<strong>Type:</strong> ${escapeHtml(resource.type)}`;

    const image = document.createElement('img');

    image.src = resourcePath || resource.path;
    image.alt = resource.name;
    image.style.maxWidth = '100%';
    image.style.maxHeight = '250px';
    image.style.borderRadius = '10px';
    image.style.boxShadow = '0 5px 15px rgba(0,0,0,.2)';
    image.style.marginTop = '10px';

    wrapper.append(title, id, type, image);
    container.appendChild(wrapper);

    requestAnimationFrame(() => {
        container.classList.add('loaded');
    });
}

function createArrow(fromId, toId, isReturn = false) {
    const fromElement = document.getElementById(fromId);
    const toElement = document.getElementById(toId);
    const diagram = document.getElementById('networkDiagram');

    if (!fromElement || !toElement || !diagram) {
        return null;
    }

    clearAllArrows();

    const fromRect = fromElement.getBoundingClientRect();
    const toRect = toElement.getBoundingClientRect();
    const diagramRect = diagram.getBoundingClientRect();

    const fromCenterX =
        fromRect.left + fromRect.width / 2 - diagramRect.left;

    const fromCenterY =
        fromRect.top + fromRect.height / 2 - diagramRect.top;

    const toCenterX =
        toRect.left + toRect.width / 2 - diagramRect.left;

    const toCenterY =
        toRect.top + toRect.height / 2 - diagramRect.top;

    const deltaX = toCenterX - fromCenterX;
    const deltaY = toCenterY - fromCenterY;

    const distance =
        Math.sqrt(deltaX * deltaX + deltaY * deltaY);

    if (distance < 50) {
        return null;
    }

    const directionX = deltaX / distance;
    const directionY = deltaY / distance;

    const startX = fromCenterX + directionX * 80;
    const startY = fromCenterY + directionY * 80;

    const endX = toCenterX - directionX * 80;
    const endY = toCenterY - directionY * 80;

    const finalDeltaX = endX - startX;
    const finalDeltaY = endY - startY;

    const finalLength =
        Math.sqrt(
            finalDeltaX * finalDeltaX
            + finalDeltaY * finalDeltaY
        );

    const angle =
        Math.atan2(finalDeltaY, finalDeltaX)
        * 180 / Math.PI;

    const arrow = document.createElement('div');

    arrow.className =
        isReturn
            ? 'arrow return'
            : 'arrow';

    arrow.style.left = `${startX}px`;
    arrow.style.top = `${startY - 2}px`;
    arrow.style.width = '0px';
    arrow.style.transform = `rotate(${angle}deg)`;

    diagram.appendChild(arrow);

    requestAnimationFrame(() => {
        arrow.style.transition = 'width 0.8s ease-out';
        arrow.style.width = `${finalLength}px`;
    });

    arrow.removeTimeout = setTimeout(() => {
        arrow.remove();
    }, 1200);

    return arrow;
}

function clearAllArrows() {
    const diagram = document.getElementById('networkDiagram');

    if (!diagram) {
        return;
    }

    diagram.querySelectorAll('.arrow').forEach(arrow => {
        if (arrow.removeTimeout) {
            clearTimeout(arrow.removeTimeout);
        }

        arrow.remove();
    });
}

function activateServer(serverId, isHit = false) {
    resetServerStates();

    const server = document.getElementById(serverId);

    if (!server) {
        return;
    }

    server.classList.add(
        isHit ? 'hit' : 'active'
    );
}

function resetServerStates() {
    document.querySelectorAll('.server-node')
        .forEach(node => {
            node.classList.remove('active', 'hit');
        });
}

function addTraceEntry(message, isHit = null) {
    const traceLog = document.getElementById('traceLog');

    const entry = document.createElement('div');
    entry.className = 'trace-entry';

    entry.textContent =
        `${new Date().toLocaleTimeString()} - ${message}`;

    if (isHit === true) {
        entry.classList.add('hit');
    } else if (isHit === false) {
        entry.classList.add('miss');
    }

    traceLog.appendChild(entry);
    traceLog.scrollTop = traceLog.scrollHeight;
}

function updateStats() {
    document.getElementById('totalRequests').textContent =
        state.totalRequests;

    document.getElementById('cacheHits').textContent =
        state.cacheHits;

    document.getElementById('cacheMisses').textContent =
        state.cacheMisses;

    const hitRatio =
        state.totalRequests === 0
            ? 0
            : Math.round(
                state.cacheHits
                / state.totalRequests
                * 100
            );

    document.getElementById('hitRatio').textContent =
        `${hitRatio}%`;

    document.getElementById('edgeARequests').textContent =
        state.edgeARequests;

    document.getElementById('edgeBRequests').textContent =
        state.edgeBRequests;

    document.getElementById('replicaUsRequests').textContent =
        state.replicaUsRequests;

    document.getElementById('replicaEuRequests').textContent =
        state.replicaEuRequests;
}

function setButtonsDisabled(disabled) {
    document.getElementById('requestBtn').disabled =
        disabled;

    document.getElementById('clearCacheBtn').disabled =
        disabled;

    document.getElementById('resourceSelect').disabled =
        disabled;
}

function getClientId() {
    const storageKey = 'cdnSimulatorClientId';

    let clientId =
        sessionStorage.getItem(storageKey);

    if (!clientId) {
        clientId =
            typeof crypto !== 'undefined'
            && typeof crypto.randomUUID === 'function'
                ? crypto.randomUUID()
                : `client-${Date.now()}`;

        sessionStorage.setItem(
            storageKey,
            clientId
        );
    }

    return clientId;
}

function ensureSuccessfulResponse(response) {
    if (response.redirected &&
        response.url.includes('/login')) {

        window.location.href = response.url;

        throw new Error(
            'Authentication is required.'
        );
    }

    if (response.status === 401 ||
        response.status === 403) {

        window.location.href = '/login';

        throw new Error(
            'Authentication is required.'
        );
    }

    if (!response.ok) {
        throw new Error(
            `Request failed with status ${response.status}.`
        );
    }
}

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

function delay(milliseconds) {
    return new Promise(resolve =>
        setTimeout(resolve, milliseconds)
    );
}