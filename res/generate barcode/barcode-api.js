// barcode type
const Barcode = {
    EAN8: "ean8",
    EAN13: "ean13",
    UPC: "upc",
    STD25: "std25",
    INT25: "int25",
    CODE11: "code11",
    CODE39: "code39",
    CODE93: "code93",
    CODE128: "code128",
    CODABAR: "codabar",
    MSI: "msi",
    MATRIX: "datamatrix"
}

var settings = {
    output: "css",
    bgColor: "#FFFFFF",
    color: "#000000",
    barWidth: 1,
    barHeight: 50,
    moduleSize: 5,
    posX: 10,
    posY: 20,
    addQuietZone: 1
};

function showBarcodeCSS(value, btype, jqID) {
    settings.output = "css";
    var val = value;

    if (btype == Barcode.MATRIX) {
        val = {
            code: value,
            rect: true
        };
    }

    $('#' + jqID).html("").show().barcode(val, btype, settings);
}

function showBarcodeCanvas(value, btype, jqID) {
    settings.output = "canvas";
    var val = value;

    if (btype == Barcode.MATRIX) {
        val = {
            code: value,
            rect: true
        };
    }
    var canvas = $('#' + jqID).get(0);
    var ctx = canvas.getContext('2d');
    ctx.lineWidth = 1;
    ctx.lineCap = 'butt';
    ctx.fillStyle = '#FFFFFF';
    ctx.strokeStyle = '#000000';
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.strokeRect(0, 0, canvas.width, canvas.height);
    $('#' + jqID).show().barcode(val, btype, settings);
}

function showQRCodeCanvas(value, jqID, size) {
    $('#' + jqID).qrcode({
        text: value,
        width: size,
        height: size
    });
}

function showQRCodeTable(value, jqID, size) {
    $('#' + jqID).qrcode({
        render: "table",
        text: value,
        width: size,
        height: size
    });
}