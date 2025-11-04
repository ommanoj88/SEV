' ====================================================================
' COMMERCIAL EV FLEET MANAGEMENT PLATFORM - POWERPOINT GENERATOR
' ====================================================================
' This VBA code generates a comprehensive PowerPoint presentation
' Author: Auto-generated for Product Concept Document
' Date: October 19, 2025
' Version: 1.0
' ====================================================================

Option Explicit

' Global Variables
Dim pptApp As Object
Dim pptPres As Object
Dim pptSlide As Object
Dim slideIndex As Integer

' Color Scheme Variables (Professional EV Theme) - Using variables instead of constants for RGB
Dim COLOR_PRIMARY As Long
Dim COLOR_SECONDARY As Long
Dim COLOR_ACCENT As Long
Dim COLOR_DARK As Long
Dim COLOR_TEXT As Long
Dim COLOR_LIGHT As Long
Dim COLOR_SUCCESS As Long
Dim COLOR_WARNING As Long
Dim COLOR_DANGER As Long

' Font Constants
Const FONT_TITLE = "Segoe UI"
Const FONT_BODY = "Segoe UI"
Const FONT_SIZE_TITLE = 44
Const FONT_SIZE_SUBTITLE = 28
Const FONT_SIZE_HEADING = 32
Const FONT_SIZE_BODY = 18
Const FONT_SIZE_CAPTION = 14

' Layout Constants
Const SLIDE_WIDTH = 960
Const SLIDE_HEIGHT = 540
Const MARGIN_LEFT = 50
Const MARGIN_TOP = 80
Const MARGIN_RIGHT = 50
Const MARGIN_BOTTOM = 50

' ====================================================================
' MAIN EXECUTION FUNCTION
' ====================================================================
Sub GenerateCompletePresentation()
    On Error GoTo ErrorHandler
    
    ' Initialize PowerPoint
    InitializePowerPoint
    
    ' Status message
    MsgBox "Starting presentation generation. This will take a few minutes. Progress will be shown.", vbInformation, "EV Fleet Management PPT Generator"
    
    ' Generate all sections
    Call CreateTitleSlide
    Call CreateAgendaSlide
    
    ' Call section generation functions as we build them
    Call CreateSection1_ExecutiveSummary
    Call CreateSection1_ProblemStatement
    Call CreateSection2_MarketOpportunity
    Call CreateSection3_TargetCustomers
    Call CreateSection4_SolutionOverview
    
    ' Final touches
    Call CreateThankYouSlide
    
    ' Make PowerPoint visible
    pptApp.Visible = True
    pptPres.Windows(1).Activate
    
    MsgBox "Presentation generated successfully with " & slideIndex & " slides!", vbInformation, "Complete"
    
    Exit Sub
    
ErrorHandler:
    MsgBox "Error: " & Err.Description, vbCritical, "Generation Error"
    If Not pptApp Is Nothing Then
        pptApp.Visible = True
    End If
End Sub

' ====================================================================
' INITIALIZATION FUNCTIONS
' ====================================================================
Sub InitializePowerPoint()
    ' Initialize color scheme
    COLOR_PRIMARY = RGB(0, 128, 96)        ' Electric Green
    COLOR_SECONDARY = RGB(0, 102, 204)     ' Electric Blue
    COLOR_ACCENT = RGB(255, 152, 0)        ' Orange
    COLOR_DARK = RGB(33, 33, 33)           ' Dark Gray
    COLOR_TEXT = RGB(66, 66, 66)           ' Text Gray
    COLOR_LIGHT = RGB(240, 240, 240)       ' Light Background
    COLOR_SUCCESS = RGB(76, 175, 80)       ' Green
    COLOR_WARNING = RGB(255, 193, 7)       ' Yellow
    COLOR_DANGER = RGB(244, 67, 54)        ' Red
    
    ' Use the current PowerPoint application
    Set pptApp = Application
    
    ' Create new presentation
    Set pptPres = pptApp.Presentations.Add
    
    ' Verify presentation was created
    If pptPres Is Nothing Then
        MsgBox "Failed to create presentation!", vbCritical
        Exit Sub
    End If
    
    ' Set slide size (16:9 widescreen)
    pptPres.PageSetup.SlideWidth = SLIDE_WIDTH
    pptPres.PageSetup.SlideHeight = SLIDE_HEIGHT
    
    ' Initialize slide counter
    slideIndex = 0
End Sub

' ====================================================================
' UTILITY FUNCTIONS
' ====================================================================

' Function to add a new slide with specified layout
Function AddSlide(layoutType As Integer) As Object
    If pptPres Is Nothing Then
        MsgBox "Presentation object is not initialized!", vbCritical, "Error"
        Exit Function
    End If
    
    slideIndex = slideIndex + 1
    Set AddSlide = pptPres.Slides.Add(slideIndex, layoutType)
End Function

' Function to add title to slide
Sub AddSlideTitle(slide As Object, titleText As String)
    Dim titleShape As Object
    Set titleShape = slide.Shapes.AddTextbox(1, MARGIN_LEFT, 30, SLIDE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT, 60)
    
    With titleShape.TextFrame.TextRange
        .Text = titleText
        .Font.Name = FONT_TITLE
        .Font.Size = FONT_SIZE_HEADING
        .Font.Bold = True
        .Font.Color.RGB = COLOR_PRIMARY
        .ParagraphFormat.Alignment = 1 ' ppAlignLeft
    End With
End Sub

' Function to add body text
Function AddBodyText(slide As Object, left As Single, top As Single, width As Single, height As Single, bodyText As String) As Object
    Dim textShape As Object
    Set textShape = slide.Shapes.AddTextbox(1, left, top, width, height)
    
    With textShape.TextFrame
        .TextRange.Text = bodyText
        .TextRange.Font.Name = FONT_BODY
        .TextRange.Font.Size = FONT_SIZE_BODY
        .TextRange.Font.Color.RGB = COLOR_TEXT
        .WordWrap = True
        .AutoSize = 1 ' ppAutoSizeShapeToFitText
    End With
    
    Set AddBodyText = textShape
End Function

' Function to add bullet points
Sub AddBulletList(slide As Object, left As Single, top As Single, width As Single, height As Single, bullets() As String)
    Dim textShape As Object
    Dim bulletText As String
    Dim i As Integer
    
    ' Build bullet text
    bulletText = ""
    For i = LBound(bullets) To UBound(bullets)
        If i > LBound(bullets) Then bulletText = bulletText & vbCrLf
        bulletText = bulletText & bullets(i)
    Next i
    
    Set textShape = slide.Shapes.AddTextbox(1, left, top, width, height)
    
    With textShape.TextFrame
        .TextRange.Text = bulletText
        .TextRange.Font.Name = FONT_BODY
        .TextRange.Font.Size = FONT_SIZE_BODY
        .TextRange.Font.Color.RGB = COLOR_TEXT
        .WordWrap = True
        
        ' Apply bullets
        With .TextRange.ParagraphFormat
            .Bullet.Visible = True
            .Bullet.Type = 1 ' ppBulletUnnumbered
            .Bullet.Character = 8226
            .LeftIndent = 20
            .FirstLineIndent = -20
        End With
    End With
End Sub

' Function to add a colored box/rectangle
Function AddColorBox(slide As Object, left As Single, top As Single, width As Single, height As Single, fillColor As Long, Optional borderColor As Long = -1) As Object
    Dim boxShape As Object
    Set boxShape = slide.Shapes.AddShape(1, left, top, width, height) ' msoShapeRectangle = 1
    
    With boxShape
        .Fill.ForeColor.RGB = fillColor
        .Fill.Solid
        
        If borderColor <> -1 Then
            .Line.Visible = True
            .Line.ForeColor.RGB = borderColor
            .Line.Weight = 2
        Else
            .Line.Visible = False
        End If
    End With
    
    Set AddColorBox = boxShape
End Function

' Function to add a rounded rectangle with text (card style)
Sub AddInfoCard(slide As Object, left As Single, top As Single, width As Single, height As Single, titleText As String, bodyText As String, bgColor As Long)
    Dim cardShape As Object
    Set cardShape = slide.Shapes.AddShape(5, left, top, width, height) ' msoShapeRoundedRectangle = 5
    
    With cardShape
        .Fill.ForeColor.RGB = bgColor
        .Fill.Solid
        .Line.Visible = False
        
        With .TextFrame
            .TextRange.Text = titleText & vbCrLf & vbCrLf & bodyText
            .TextRange.Font.Name = FONT_BODY
            .TextRange.Font.Size = FONT_SIZE_BODY - 2
            .WordWrap = True
            .MarginLeft = 15
            .MarginRight = 15
            .MarginTop = 15
            .MarginBottom = 15
            
            ' Format title (first line)
            With .TextRange.Paragraphs(1)
                .Font.Bold = True
                .Font.Size = FONT_SIZE_BODY
                .Font.Color.RGB = COLOR_PRIMARY
            End With
            
            ' Format body
            .TextRange.Font.Color.RGB = COLOR_TEXT
        End With
    End With
End Sub

' ====================================================================
' TITLE & INTRO SLIDES
' ====================================================================

Sub CreateTitleSlide()
    Dim slide As Object
    Set slide = AddSlide(12) ' ppLayoutBlank
    
    ' Set slide background color using Shape method
    Dim bgShape As Object
    Set bgShape = slide.Shapes.AddShape(1, 0, 0, SLIDE_WIDTH, SLIDE_HEIGHT)
    With bgShape
        .Fill.ForeColor.RGB = COLOR_PRIMARY
        .Fill.Solid
        .Line.Visible = False
    End With
    ' Send background shape to back
    slide.Shapes(slide.Shapes.Count).ZOrder 2
    
    ' Main Title
    Dim titleShape As Object
    Set titleShape = slide.Shapes.AddTextbox(1, 80, 150, 800, 120)
    With titleShape.TextFrame.TextRange
        .Text = "Commercial EV Fleet" & vbCrLf & "Management Platform"
        .Font.Name = FONT_TITLE
        .Font.Size = 54
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
        .ParagraphFormat.Alignment = 2 ' ppAlignCenter
    End With
    
    ' Subtitle
    Dim subtitleShape As Object
    Set subtitleShape = slide.Shapes.AddTextbox(1, 80, 300, 800, 60)
    With subtitleShape.TextFrame.TextRange
        .Text = "Empowering India's Logistics Revolution with Intelligent, Sustainable, and Scalable Solutions"
        .Font.Name = FONT_BODY
        .Font.Size = 20
        .Font.Color.RGB = RGB(220, 240, 235)
        .ParagraphFormat.Alignment = 2 ' ppAlignCenter
    End With
    
    ' Version and Date
    Dim infoShape As Object
    Set infoShape = slide.Shapes.AddTextbox(1, 300, 420, 360, 40)
    With infoShape.TextFrame.TextRange
        .Text = "Version 1.0 | October 19, 2025"
        .Font.Name = FONT_BODY
        .Font.Size = 16
        .Font.Color.RGB = RGB(200, 220, 210)
        .ParagraphFormat.Alignment = 2 ' ppAlignCenter
    End With
    
    ' Decorative elements
    Call AddColorBox(slide, 400, 100, 160, 8, RGB(255, 152, 0))
End Sub

Sub CreateAgendaSlide()
    Dim slide As Object
    Set slide = AddSlide(12) ' ppLayoutBlank
    
    Call AddSlideTitle(slide, "Agenda")
    
    ' Agenda items in two columns
    Dim agendaItems() As String
    ReDim agendaItems(1 To 14)
    agendaItems(1) = "1. Executive Summary & Problem Statement"
    agendaItems(2) = "2. Market Opportunity & Timing"
    agendaItems(3) = "3. Target Customers & Personas"
    agendaItems(4) = "4. Proposed Solution Overview"
    agendaItems(5) = "5. Key Features & Functionality"
    agendaItems(6) = "6. Technology Stack & Architecture"
    agendaItems(7) = "7. Business Model & Pricing"
    agendaItems(8) = "8. Competitive Landscape"
    agendaItems(9) = "9. Impact & Value Proposition"
    agendaItems(10) = "10. Future Roadmap"
    agendaItems(11) = "11. Implementation Plan"
    agendaItems(12) = "12. Success Metrics & KPIs"
    agendaItems(13) = "13. Risk Assessment"
    agendaItems(14) = "14. Conclusion"
    
    ' Create cards for each section
    Dim i As Integer
    Dim row As Integer
    Dim col As Integer
    Dim cardLeft As Single
    Dim cardTop As Single
    Dim cardWidth As Single
    Dim cardHeight As Single
    
    cardWidth = 420
    cardHeight = 50
    
    For i = 1 To 14
        row = (i - 1) Mod 7
        col = IIf(i <= 7, 0, 1)
        
        cardLeft = MARGIN_LEFT + (col * (cardWidth + 20))
        cardTop = 110 + (row * (cardHeight + 8))
        
        Dim agendaBox As Object
        Set agendaBox = AddColorBox(slide, cardLeft, cardTop, cardWidth, cardHeight, COLOR_LIGHT)
        
        Dim agendaText As Object
        Set agendaText = slide.Shapes.AddTextbox(1, cardLeft + 15, cardTop + 12, cardWidth - 30, cardHeight - 24)
        With agendaText.TextFrame.TextRange
            .Text = agendaItems(i)
            .Font.Name = FONT_BODY
            .Font.Size = 14
            .Font.Color.RGB = COLOR_TEXT
            .Font.Bold = False
        End With
    Next i
End Sub

Sub CreateThankYouSlide()
    Dim slide As Object
    Set slide = AddSlide(12) ' ppLayoutBlank
    
    ' Set slide background color using Shape method
    Dim bgShape As Object
    Set bgShape = slide.Shapes.AddShape(1, 0, 0, SLIDE_WIDTH, SLIDE_HEIGHT)
    With bgShape
        .Fill.ForeColor.RGB = COLOR_SECONDARY
        .Fill.Solid
        .Line.Visible = False
    End With
    ' Send background shape to back
    slide.Shapes(slide.Shapes.Count).ZOrder 2
    
    ' Thank You text
    Dim titleShape As Object
    Set titleShape = slide.Shapes.AddTextbox(1, 100, 180, 760, 100)
    With titleShape.TextFrame.TextRange
        .Text = "Thank You"
        .Font.Name = FONT_TITLE
        .Font.Size = 66
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
        .ParagraphFormat.Alignment = 2 ' ppAlignCenter
    End With
    
    ' Contact information
    Dim contactShape As Object
    Set contactShape = slide.Shapes.AddTextbox(1, 100, 320, 760, 80)
    With contactShape.TextFrame.TextRange
        .Text = "Ready to Transform Your EV Fleet?" & vbCrLf & "Let's Connect"
        .Font.Name = FONT_BODY
        .Font.Size = 24
        .Font.Color.RGB = RGB(220, 240, 255)
        .ParagraphFormat.Alignment = 2 ' ppAlignCenter
    End With
End Sub

' ====================================================================
' PROGRESS TRACKING
' ====================================================================
Sub ShowProgress(sectionName As String)
    ' Progress notification (PowerPoint doesn't have a status bar like Excel)
    ' You can optionally add a DoEvents here to keep UI responsive
    DoEvents
End Sub

' ====================================================================
' SECTION 1: EXECUTIVE SUMMARY & PROBLEM STATEMENT
' ====================================================================

Sub CreateSection1_ExecutiveSummary()
    Call ShowProgress("Section 1 - Executive Summary")
    
    Dim slide As Object
    Set slide = AddSlide(12) ' ppLayoutBlank
    
    ' Section divider design
    Call AddColorBox(slide, 0, 0, SLIDE_WIDTH, 120, COLOR_PRIMARY)
    
    Dim sectionTitle As Object
    Set sectionTitle = slide.Shapes.AddTextbox(1, MARGIN_LEFT, 35, 700, 60)
    With sectionTitle.TextFrame.TextRange
        .Text = "EXECUTIVE SUMMARY"
        .Font.Name = FONT_TITLE
        .Font.Size = 42
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
    End With
    
    ' Subtitle
    Dim subtitle As Object
    Set subtitle = slide.Shapes.AddTextbox(1, MARGIN_LEFT, 140, 860, 40)
    With subtitle.TextFrame.TextRange
        .Text = "Cloud-Native B2B SaaS Platform for Indian EV Fleet Management"
        .Font.Name = FONT_BODY
        .Font.Size = 22
        .Font.Bold = True
        .Font.Color.RGB = COLOR_PRIMARY
    End With
    
    ' Main content boxes
    Call AddInfoCard(slide, MARGIN_LEFT, 200, 420, 130, "THE CHALLENGE", _
        "Indian logistics companies face fragmented charging networks, data silos, reactive maintenance, and lack of EV-specific management tools.", _
        RGB(255, 243, 224))
    
    Call AddInfoCard(slide, 490, 200, 420, 130, "THE SOLUTION", _
        "Unified platform integrating real-time telematics, charging orchestration, predictive maintenance, and business intelligence.", _
        RGB(232, 245, 233))
    
    Call AddInfoCard(slide, MARGIN_LEFT, 350, 420, 130, "THE OPPORTUNITY", _
        "800K-1M commercial EVs by 2027 in India. $650-850M TAM by 2030. No strong India-centric competitor.", _
        RGB(227, 242, 253))
    
    Call AddInfoCard(slide, 490, 350, 420, 130, "THE IMPACT", _
        "15-25% downtime reduction, 8-12% cost savings, automated ESG reporting, 5-10x ROI.", _
        RGB(248, 231, 248))
End Sub

Sub CreateSection1_ProblemStatement()
    Call ShowProgress("Section 1 - Problem Statement")
    
    ' Slide 1: Problem Statement Overview
    Dim slide1 As Object
    Set slide1 = AddSlide(12)
    
    Call AddSlideTitle(slide1, "Problem Statement: Current Challenges")
    
    ' Four major challenge categories
    Dim challenges(1 To 4) As String
    Dim challengeTitles(1 To 4) As String
    
    challengeTitles(1) = "Infrastructure & Charging"
    challenges(1) = "Fragmented charging networks" & vbCrLf & "Range anxiety & route planning" & vbCrLf & "15-20% productivity loss" & vbCrLf & "Energy cost unpredictability"
    
    challengeTitles(2) = "Data Fragmentation"
    challenges(2) = "Manual spreadsheet tracking" & vbCrLf & "No real-time visibility" & vbCrLf & "Disconnected systems" & vbCrLf & "Poor decision-making"
    
    challengeTitles(3) = "Maintenance & Battery"
    challenges(3) = "Reactive maintenance only" & vbCrLf & "Battery degradation uncertainty" & vbCrLf & "Limited OEM integration" & vbCrLf & "Complex warranty tracking"
    
    challengeTitles(4) = "Regulatory & Compliance"
    challenges(4) = "Complex sustainability reporting" & vbCrLf & "FAME II subsidy documentation" & vbCrLf & "Driver safety monitoring" & vbCrLf & "Compliance management"
    
    Dim i As Integer
    Dim row As Integer
    Dim col As Integer
    Dim cardLeft As Single
    Dim cardTop As Single
    
    For i = 1 To 4
        row = IIf(i <= 2, 0, 1)
        col = IIf(i Mod 2 = 1, 0, 1)
        
        cardLeft = MARGIN_LEFT + (col * 440)
        cardTop = 120 + (row * 190)
        
        ' Icon background circle
        Dim iconCircle As Object
        Set iconCircle = slide1.Shapes.AddShape(9, cardLeft, cardTop, 50, 50) ' msoShapeOval
        iconCircle.Fill.ForeColor.RGB = IIf(i = 1, COLOR_DANGER, IIf(i = 2, COLOR_WARNING, IIf(i = 3, COLOR_SECONDARY, COLOR_PRIMARY)))
        iconCircle.Fill.Solid
        iconCircle.Line.Visible = False
        
        ' Challenge title
        Dim titleBox As Object
        Set titleBox = slide1.Shapes.AddTextbox(1, cardLeft + 60, cardTop + 5, 350, 40)
        With titleBox.TextFrame.TextRange
            .Text = challengeTitles(i)
            .Font.Name = FONT_BODY
            .Font.Size = 20
            .Font.Bold = True
            .Font.Color.RGB = COLOR_DARK
        End With
        
        ' Challenge details
        Dim detailBox As Object
        Set detailBox = slide1.Shapes.AddTextbox(1, cardLeft, cardTop + 55, 410, 120)
        With detailBox.TextFrame.TextRange
            .Text = challenges(i)
            .Font.Name = FONT_BODY
            .Font.Size = 15
            .Font.Color.RGB = COLOR_TEXT
        End With
    Next i
    
    ' Slide 2: Market Gaps
    Dim slide2 As Object
    Set slide2 = AddSlide(12)
    
    Call AddSlideTitle(slide2, "Why Current Solutions Don't Work")
    
    ' Gap 1: No India-Centric Solution
    Call AddColorBox(slide2, MARGIN_LEFT, 110, 280, 50, RGB(244, 67, 54))
    Dim gap1Title As Object
    Set gap1Title = slide2.Shapes.AddTextbox(1, MARGIN_LEFT + 10, 120, 260, 30)
    With gap1Title.TextFrame.TextRange
        .Text = "No India-Centric Solution"
        .Font.Name = FONT_BODY
        .Font.Size = 18
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
        .ParagraphFormat.Alignment = 2
    End With
    
    Dim gap1Bullets(1 To 3) As String
    gap1Bullets(1) = "Global platforms (Geotab, Samsara, Fleetio) too expensive"
    gap1Bullets(2) = "No integration with Indian charging networks"
    gap1Bullets(3) = "Don't support 2W/3W vehicles common in India"
    Call AddBulletList(slide2, MARGIN_LEFT, 170, 280, 100, gap1Bullets)
    
    ' Gap 2: Legacy Systems Don't Support EVs
    Call AddColorBox(slide2, 350, 110, 280, 50, RGB(255, 152, 0))
    Dim gap2Title As Object
    Set gap2Title = slide2.Shapes.AddTextbox(1, 360, 120, 260, 30)
    With gap2Title.TextFrame.TextRange
        .Text = "Legacy Systems Don't Support EVs"
        .Font.Name = FONT_BODY
        .Font.Size = 18
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
        .ParagraphFormat.Alignment = 2
    End With
    
    Dim gap2Bullets(1 To 3) As String
    gap2Bullets(1) = "Built for ICE vehicles, not electric"
    gap2Bullets(2) = "No battery health monitoring or SoC tracking"
    gap2Bullets(3) = "No charging infrastructure management"
    Call AddBulletList(slide2, 350, 170, 280, 100, gap2Bullets)
    
    ' Gap 3: Lack of Ecosystem Integration
    Call AddColorBox(slide2, 650, 110, 280, 50, RGB(0, 128, 96))
    Dim gap3Title As Object
    Set gap3Title = slide2.Shapes.AddTextbox(1, 660, 120, 260, 30)
    With gap3Title.TextFrame.TextRange
        .Text = "Lack of Ecosystem Integration"
        .Font.Name = FONT_BODY
        .Font.Size = 18
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
        .ParagraphFormat.Alignment = 2
    End With
    
    Dim gap3Bullets(1 To 3) As String
    gap3Bullets(1) = "No platform integrates OEM telematics"
    gap3Bullets(2) = "Missing charging network API connections"
    gap3Bullets(3) = "No ERP/payment gateway integration"
    Call AddBulletList(slide2, 650, 170, 280, 100, gap3Bullets)
    
    ' Bottom impact statement
    Call AddColorBox(slide2, MARGIN_LEFT, 300, 860, 160, COLOR_LIGHT)
    Dim impactTitle As Object
    Set impactTitle = slide2.Shapes.AddTextbox(1, MARGIN_LEFT + 20, 315, 820, 35)
    With impactTitle.TextFrame.TextRange
        .Text = "THE RESULT: Companies Struggle to Transition to EVs"
        .Font.Name = FONT_BODY
        .Font.Size = 22
        .Font.Bold = True
        .Font.Color.RGB = COLOR_PRIMARY
        .ParagraphFormat.Alignment = 2
    End With
    
    Dim impactBullets(1 To 4) As String
    impactBullets(1) = "⚡ 15-20% vehicle downtime due to charging inefficiencies"
    impactBullets(2) = "💰 Unpredictable operating costs and poor ROI visibility"
    impactBullets(3) = "📊 No data to justify fleet expansion or optimization"
    impactBullets(4) = "🌍 Unable to track and report sustainability achievements"
    Call AddBulletList(slide2, MARGIN_LEFT + 40, 360, 820, 90, impactBullets)
End Sub

' ====================================================================
' SECTION 2: MARKET OPPORTUNITY & TIMING
' ====================================================================

Sub CreateSection2_MarketOpportunity()
    Call ShowProgress("Section 2 - Market Opportunity")
    
    ' Slide 1: Market Size Overview
    Dim slide1 As Object
    Set slide1 = AddSlide(12)
    
    Call AddColorBox(slide1, 0, 0, SLIDE_WIDTH, 100, RGB(0, 102, 204))
    
    Dim sectionTitle As Object
    Set sectionTitle = slide1.Shapes.AddTextbox(1, MARGIN_LEFT, 30, 700, 50)
    With sectionTitle.TextFrame.TextRange
        .Text = "MARKET OPPORTUNITY"
        .Font.Name = FONT_TITLE
        .Font.Size = 38
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
    End With
    
    ' Key market stats in cards
    Call AddInfoCard(slide1, MARGIN_LEFT, 120, 200, 120, "38-42%", "CAGR Growth" & vbCrLf & "2024-2030", RGB(232, 245, 233))
    Call AddInfoCard(slide1, MARGIN_LEFT + 220, 120, 200, 120, "800K-1M", "Commercial EVs" & vbCrLf & "by 2027", RGB(255, 243, 224))
    Call AddInfoCard(slide1, MARGIN_LEFT + 440, 120, 200, 120, "$650-850M", "India TAM" & vbCrLf & "by 2030", RGB(227, 242, 253))
    Call AddInfoCard(slide1, MARGIN_LEFT + 660, 120, 200, 120, "$52.4B", "Global Market" & vbCrLf & "by 2030", RGB(248, 231, 248))
    
    ' Make the numbers larger and bold
    Dim i As Integer
    For i = slide1.Shapes.Count - 3 To slide1.Shapes.Count
        With slide1.Shapes(i).TextFrame.TextRange.Paragraphs(1)
            .Font.Size = 32
            .Font.Bold = True
            .Font.Color.RGB = COLOR_PRIMARY
        End With
    Next i
    
    ' Government Support Section
    Call AddColorBox(slide1, MARGIN_LEFT, 260, 860, 40, COLOR_PRIMARY)
    Dim govTitle As Object
    Set govTitle = slide1.Shapes.AddTextbox(1, MARGIN_LEFT + 20, 268, 820, 25)
    With govTitle.TextFrame.TextRange
        .Text = "GOVERNMENT PUSH: Strong Policy Support"
        .Font.Name = FONT_BODY
        .Font.Size = 20
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
    End With
    
    Dim govBullets(1 To 5) As String
    govBullets(1) = "FAME II extended with ₹2,671 crore allocation (Budget 2024-25)"
    govBullets(2) = "PM E-Bus Seva: 10,000 e-buses across 169 cities"
    govBullets(3) = "EMPS 2024 launched with ₹500 crore outlay"
    govBullets(4) = "State EV mandates in Delhi, Maharashtra, Karnataka, Tamil Nadu"
    govBullets(5) = "PLI schemes for battery manufacturing with customs duty exemptions"
    Call AddBulletList(slide1, MARGIN_LEFT + 30, 310, 820, 165, govBullets)
    
    ' Slide 2: E-commerce Boom
    Dim slide2 As Object
    Set slide2 = AddSlide(12)
    
    Call AddSlideTitle(slide2, "E-Commerce & Quick Commerce Driving Adoption")
    
    ' Left side: Major players
    Call AddColorBox(slide2, MARGIN_LEFT, 100, 420, 50, RGB(0, 128, 96))
    Dim playersTitle As Object
    Set playersTitle = slide2.Shapes.AddTextbox(1, MARGIN_LEFT + 10, 110, 400, 30)
    With playersTitle.TextFrame.TextRange
        .Text = "Major Players Transitioning to EVs"
        .Font.Name = FONT_BODY
        .Font.Size = 20
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
    End With
    
    Dim players(1 To 7) As String
    players(1) = "Zomato & Swiggy: Massive EV fleet deployment"
    players(2) = "Amazon India: 10,000 EVs by 2025 commitment"
    players(3) = "Flipkart: Partnering with OEMs for expansion"
    players(4) = "Zepto & Blinkit: Quick commerce EV fleets"
    players(5) = "BigBasket & Dunzo: Last-mile EV transition"
    players(6) = "Dark stores increasing 2W/3W EV demand"
    players(7) = "Quick commerce market: $5.5B by 2025"
    Call AddBulletList(slide2, MARGIN_LEFT + 10, 160, 400, 310, players)
    
    ' Right side: Infrastructure Growth
    Call AddColorBox(slide2, 490, 100, 420, 50, RGB(0, 102, 204))
    Dim infraTitle As Object
    Set infraTitle = slide2.Shapes.AddTextbox(1, 500, 110, 400, 30)
    With infraTitle.TextFrame.TextRange
        .Text = "Infrastructure Maturity"
        .Font.Name = FONT_BODY
        .Font.Size = 20
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
    End With
    
    ' Infrastructure stats boxes
    Call AddInfoCard(slide2, 500, 160, 190, 100, "12,000+", "Public charging" & vbCrLf & "stations (2024)", RGB(227, 242, 253))
    Call AddInfoCard(slide2, 710, 160, 190, 100, "30,000+", "Projected by" & vbCrLf & "2027", RGB(232, 245, 233))
    
    With slide2.Shapes(slide2.Shapes.Count - 1).TextFrame.TextRange.Paragraphs(1)
        .Font.Size = 28
        .Font.Bold = True
        .Font.Color.RGB = COLOR_SECONDARY
    End With
    With slide2.Shapes(slide2.Shapes.Count).TextFrame.TextRange.Paragraphs(1)
        .Font.Size = 28
        .Font.Bold = True
        .Font.Color.RGB = COLOR_SUCCESS
    End With
    
    Dim infraBullets(1 To 5) As String
    infraBullets(1) = "Tata Power EZ: 5,800+ points in 550+ cities"
    infraBullets(2) = "Multiple networks: Fortum, Statiq, Ather Grid"
    infraBullets(3) = "Battery costs: $140/kWh → $115-125/kWh"
    infraBullets(4) = "Approaching ICE vehicle parity"
    infraBullets(5) = "Kazam EV, GO EC expanding networks"
    Call AddBulletList(slide2, 500, 270, 400, 200, infraBullets)
    
    ' Slide 3: Why Now?
    Dim slide3 As Object
    Set slide3 = AddSlide(12)
    
    Call AddSlideTitle(slide3, "Why Now? Perfect Market Timing")
    
    Dim reasons(1 To 4) As String
    Dim reasonTitles(1 To 4) As String
    Dim reasonColors(1 To 4) As Long
    
    reasonTitles(1) = "Infrastructure Ready"
    reasons(1) = "30,000+ charging stations by 2027" & vbCrLf & "Battery costs at parity with ICE" & vbCrLf & "Multiple charging network operators"
    reasonColors(1) = RGB(232, 245, 233)
    
    reasonTitles(2) = "Digital Adoption"
    reasons(2) = "SMEs comfortable with cloud SaaS" & vbCrLf & "Demand for data-driven decisions" & vbCrLf & "Mobile-first workforce ready"
    reasonColors(2) = RGB(227, 242, 253)
    
    reasonTitles(3) = "Competitive Vacuum"
    reasons(3) = "No strong India-centric player" & vbCrLf & "Global players slow to adapt" & vbCrLf & "First-mover advantage available"
    reasonColors(3) = RGB(255, 243, 224)
    
    reasonTitles(4) = "ESG Pressure"
    reasons(4) = "Corporate sustainability mandates" & vbCrLf & "Investor demand for ESG reporting" & vbCrLf & "Carbon neutrality commitments"
    reasonColors(4) = RGB(248, 231, 248)
    
    For i = 1 To 4
        Dim reasonRow As Integer
        Dim reasonCol As Integer
        Dim reasonLeft As Single
        Dim reasonTop As Single
        
        reasonRow = IIf(i <= 2, 0, 1)
        reasonCol = IIf(i Mod 2 = 1, 0, 1)
        
        reasonLeft = MARGIN_LEFT + (reasonCol * 440)
        reasonTop = 110 + (reasonRow * 180)
        
        Call AddInfoCard(slide3, reasonLeft, reasonTop, 420, 160, reasonTitles(i), reasons(i), reasonColors(i))
    Next i
    
    ' Bottom conclusion
    Call AddColorBox(slide3, MARGIN_LEFT, 470, 860, 50, COLOR_PRIMARY)
    Dim conclusion As Object
    Set conclusion = slide3.Shapes.AddTextbox(1, MARGIN_LEFT + 20, 480, 820, 30)
    With conclusion.TextFrame.TextRange
        .Text = "PERFECT STORM: Technology + Policy + Demand Converging NOW"
        .Font.Name = FONT_BODY
        .Font.Size = 22
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
        .ParagraphFormat.Alignment = 2
    End With
End Sub

' ====================================================================
' SECTION 3: TARGET CUSTOMERS & PERSONAS
' ====================================================================

Sub CreateSection3_TargetCustomers()
    Call ShowProgress("Section 3 - Target Customers")
    
    ' Slide 1: Customer Segments Overview
    Dim slide1 As Object
    Set slide1 = AddSlide(12)
    
    Call AddColorBox(slide1, 0, 0, SLIDE_WIDTH, 100, RGB(255, 87, 34))
    
    Dim sectionTitle As Object
    Set sectionTitle = slide1.Shapes.AddTextbox(1, MARGIN_LEFT, 30, 700, 50)
    With sectionTitle.TextFrame.TextRange
        .Text = "TARGET CUSTOMERS"
        .Font.Name = FONT_TITLE
        .Font.Size = 38
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
    End With
    
    ' Primary Segments Title
    Dim primaryTitle As Object
    Set primaryTitle = slide1.Shapes.AddTextbox(1, MARGIN_LEFT, 110, 400, 30)
    With primaryTitle.TextFrame.TextRange
        .Text = "PRIMARY SEGMENTS"
        .Font.Name = FONT_BODY
        .Font.Size = 20
        .Font.Bold = True
        .Font.Color.RGB = COLOR_PRIMARY
    End With
    
    ' Segment A: Last-Mile Delivery
    Call AddInfoCard(slide1, MARGIN_LEFT, 150, 280, 150, "Last-Mile Delivery", _
        "Fleet: 500-50,000 vehicles" & vbCrLf & "Type: 2W/3W electric" & vbCrLf & _
        "Examples: Zomato, Swiggy, Blinkit, Zepto" & vbCrLf & _
        "Key Need: Real-time charging optimization", RGB(255, 243, 224))
    
    ' Segment B: Logistics & Transportation
    Call AddInfoCard(slide1, 340, 150, 280, 150, "Logistics Companies", _
        "Fleet: 100-5,000 vehicles" & vbCrLf & "Type: 3W, LCV vans/trucks" & vbCrLf & _
        "Examples: Delhivery, Ecom Express, BlueDart" & vbCrLf & _
        "Key Need: Route optimization, ERP integration", RGB(227, 242, 253))
    
    ' Segment C: Fleet Operators
    Call AddInfoCard(slide1, 630, 150, 280, 150, "EV Fleet Operators", _
        "Fleet: 1,000-20,000 vehicles" & vbCrLf & "Type: Leasing/BaaS providers" & vbCrLf & _
        "Examples: Battery swap companies" & vbCrLf & _
        "Key Need: Asset tracking, billing automation", RGB(232, 245, 233))
    
    ' Secondary Segments
    Dim secondaryTitle As Object
    Set secondaryTitle = slide1.Shapes.AddTextbox(1, MARGIN_LEFT, 315, 400, 30)
    With secondaryTitle.TextFrame.TextRange
        .Text = "SECONDARY SEGMENTS"
        .Font.Name = FONT_BODY
        .Font.Size = 18
        .Font.Bold = True
        .Font.Color.RGB = COLOR_SECONDARY
    End With
    
    ' Segment D & E in smaller cards
    Call AddInfoCard(slide1, MARGIN_LEFT, 355, 420, 115, "Corporate Fleets", _
        "50-500 vehicles | FMCG, Telecom, Pharma" & vbCrLf & "Need: Sustainability reporting, cost reduction", RGB(248, 231, 248))
    
    Call AddInfoCard(slide1, 490, 355, 420, 115, "Government & Municipal", _
        "100-2,000 vehicles | City buses, waste collection" & vbCrLf & "Need: Transparency, compliance with EV mandates", RGB(225, 245, 254))
    
    ' Slide 2: Customer Persona 1 - Fleet Manager
    Dim slide2 As Object
    Set slide2 = AddSlide(12)
    
    Call AddSlideTitle(slide2, "Customer Persona 1: Fleet Manager - Operations")
    
    ' Persona card with avatar placeholder
    Call AddColorBox(slide2, MARGIN_LEFT, 100, 400, 60, COLOR_PRIMARY)
    Dim persona1Name As Object
    Set persona1Name = slide2.Shapes.AddTextbox(1, MARGIN_LEFT + 80, 110, 300, 40)
    With persona1Name.TextFrame.TextRange
        .Text = "Rajesh Kumar - Fleet Manager"
        .Font.Name = FONT_BODY
        .Font.Size = 22
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
    End With
    
    ' Avatar circle
    Dim avatar1 As Object
    Set avatar1 = slide2.Shapes.AddShape(9, MARGIN_LEFT + 10, 105, 60, 60)
    avatar1.Fill.ForeColor.RGB = RGB(255, 255, 255)
    avatar1.Fill.Solid
    avatar1.Line.Visible = False
    
    ' Details sections
    Call AddColorBox(slide2, MARGIN_LEFT, 170, 400, 30, RGB(200, 230, 201))
    Dim role1 As Object
    Set role1 = slide2.Shapes.AddTextbox(1, MARGIN_LEFT + 10, 175, 380, 20)
    With role1.TextFrame.TextRange
        .Text = "Role: Oversees day-to-day fleet operations"
        .Font.Name = FONT_BODY
        .Font.Size = 15
        .Font.Bold = True
        .Font.Color.RGB = COLOR_DARK
    End With
    
    Dim goals1(1 To 3) As String
    goals1(1) = "Maximize vehicle uptime and utilization"
    goals1(2) = "Reduce operational costs"
    goals1(3) = "Ensure on-time deliveries"
    Dim goalsText As Object
    Set goalsText = slide2.Shapes.AddTextbox(1, MARGIN_LEFT, 210, 400, 80)
    With goalsText.TextFrame.TextRange
        .Text = "GOALS:" & vbCrLf & "• " & goals1(1) & vbCrLf & "• " & goals1(2) & vbCrLf & "• " & goals1(3)
        .Font.Name = FONT_BODY
        .Font.Size = 15
        .Font.Color.RGB = COLOR_TEXT
        .Paragraphs(1).Font.Bold = True
        .Paragraphs(1).Font.Color.RGB = COLOR_PRIMARY
    End With
    
    Dim pains1(1 To 3) As String
    pains1(1) = "Firefighting charging issues and breakdowns"
    pains1(2) = "Lack of real-time fleet visibility"
    pains1(3) = "Manual coordination with drivers"
    Dim painsText As Object
    Set painsText = slide2.Shapes.AddTextbox(1, MARGIN_LEFT, 300, 400, 80)
    With painsText.TextFrame.TextRange
        .Text = "PAIN POINTS:" & vbCrLf & "• " & pains1(1) & vbCrLf & "• " & pains1(2) & vbCrLf & "• " & pains1(3)
        .Font.Name = FONT_BODY
        .Font.Size = 15
        .Font.Color.RGB = COLOR_TEXT
        .Paragraphs(1).Font.Bold = True
        .Paragraphs(1).Font.Color.RGB = COLOR_DANGER
    End With
    
    Dim tech1 As Object
    Set tech1 = slide2.Shapes.AddTextbox(1, MARGIN_LEFT, 390, 400, 70)
    With tech1.TextFrame.TextRange
        .Text = "TECH SAVVINESS: Medium" & vbCrLf & "Needs intuitive dashboards and mobile access"
        .Font.Name = FONT_BODY
        .Font.Size = 15
        .Font.Color.RGB = COLOR_TEXT
        .Paragraphs(1).Font.Bold = True
        .Paragraphs(1).Font.Color.RGB = COLOR_SECONDARY
    End With
    
    ' Right side: Value Proposition
    Call AddColorBox(slide2, 490, 100, 420, 360, RGB(232, 245, 233))
    Dim value1Title As Object
    Set value1Title = slide2.Shapes.AddTextbox(1, 510, 110, 380, 30)
    With value1Title.TextFrame.TextRange
        .Text = "HOW WE SOLVE THEIR PROBLEMS"
        .Font.Name = FONT_BODY
        .Font.Size = 20
        .Font.Bold = True
        .Font.Color.RGB = COLOR_PRIMARY
        .ParagraphFormat.Alignment = 2
    End With
    
    Dim solutions1(1 To 6) As String
    solutions1(1) = "✓ Real-time dashboard: See all vehicles at a glance"
    solutions1(2) = "✓ Automated charging alerts: No more surprises"
    solutions1(3) = "✓ Predictive maintenance: Fix before it breaks"
    solutions1(4) = "✓ Mobile app: Manage fleet on the go"
    solutions1(5) = "✓ 50% fewer emergency calls and issues"
    solutions1(6) = "✓ 20% reduction in downtime"
    
    Dim solText As Object
    Set solText = slide2.Shapes.AddTextbox(1, 510, 150, 380, 280)
    With solText.TextFrame
        .TextRange.Text = solutions1(1) & vbCrLf & solutions1(2) & vbCrLf & solutions1(3) & vbCrLf & solutions1(4) & vbCrLf & vbCrLf & solutions1(5) & vbCrLf & solutions1(6)
        .TextRange.Font.Name = FONT_BODY
        .TextRange.Font.Size = 16
        .TextRange.Font.Color.RGB = COLOR_TEXT
        .MarginLeft = 20
        .MarginTop = 10
    End With
    
    ' Slide 3: Customer Persona 2 - COO
    Dim slide3 As Object
    Set slide3 = AddSlide(12)
    
    Call AddSlideTitle(slide3, "Customer Persona 2: Chief Operating Officer (COO)")
    
    Call AddColorBox(slide3, MARGIN_LEFT, 100, 400, 60, RGB(0, 102, 204))
    Dim persona2Name As Object
    Set persona2Name = slide3.Shapes.AddTextbox(1, MARGIN_LEFT + 80, 110, 300, 40)
    With persona2Name.TextFrame.TextRange
        .Text = "Priya Sharma - COO"
        .Font.Name = FONT_BODY
        .Font.Size = 22
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
    End With
    
    Dim avatar2 As Object
    Set avatar2 = slide2.Shapes.AddShape(9, MARGIN_LEFT + 10, 105, 60, 60)
    avatar2.Fill.ForeColor.RGB = RGB(255, 255, 255)
    avatar2.Fill.Solid
    avatar2.Line.Visible = False
    
    Call AddColorBox(slide3, MARGIN_LEFT, 170, 400, 30, RGB(187, 222, 251))
    Dim role2 As Object
    Set role2 = slide3.Shapes.AddTextbox(1, MARGIN_LEFT + 10, 175, 380, 20)
    With role2.TextFrame.TextRange
        .Text = "Role: Strategic decision-maker for operations"
        .Font.Name = FONT_BODY
        .Font.Size = 15
        .Font.Bold = True
        .Font.Color.RGB = COLOR_DARK
    End With
    
    Dim goals2Text As Object
    Set goals2Text = slide3.Shapes.AddTextbox(1, MARGIN_LEFT, 210, 400, 80)
    With goals2Text.TextFrame.TextRange
        .Text = "GOALS:" & vbCrLf & "• Improve operational efficiency" & vbCrLf & "• Demonstrate ROI on EV investment" & vbCrLf & "• Report sustainability to board"
        .Font.Name = FONT_BODY
        .Font.Size = 15
        .Font.Color.RGB = COLOR_TEXT
        .Paragraphs(1).Font.Bold = True
        .Paragraphs(1).Font.Color.RGB = COLOR_PRIMARY
    End With
    
    Dim pains2Text As Object
    Set pains2Text = slide3.Shapes.AddTextbox(1, MARGIN_LEFT, 300, 400, 80)
    With pains2Text.TextFrame.TextRange
        .Text = "PAIN POINTS:" & vbCrLf & "• Proving TCO benefits of EVs vs ICE" & vbCrLf & "• Justifying fleet expansion" & vbCrLf & "• Sustainability metrics reporting"
        .Font.Name = FONT_BODY
        .Font.Size = 15
        .Font.Color.RGB = COLOR_TEXT
        .Paragraphs(1).Font.Bold = True
        .Paragraphs(1).Font.Color.RGB = COLOR_DANGER
    End With
    
    Dim tech2 As Object
    Set tech2 = slide3.Shapes.AddTextbox(1, MARGIN_LEFT, 390, 400, 70)
    With tech2.TextFrame.TextRange
        .Text = "TECH SAVVINESS: High" & vbCrLf & "Values analytics, reporting, and data insights"
        .Font.Name = FONT_BODY
        .Font.Size = 15
        .Font.Color.RGB = COLOR_TEXT
        .Paragraphs(1).Font.Bold = True
        .Paragraphs(1).Font.Color.RGB = COLOR_SECONDARY
    End With
    
    Call AddColorBox(slide3, 490, 100, 420, 360, RGB(255, 243, 224))
    Dim value2Title As Object
    Set value2Title = slide3.Shapes.AddTextbox(1, 510, 110, 380, 30)
    With value2Title.TextFrame.TextRange
        .Text = "HOW WE DELIVER VALUE"
        .Font.Name = FONT_BODY
        .Font.Size = 20
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 87, 34)
        .ParagraphFormat.Alignment = 2
    End With
    
    Dim solutions2Text As Object
    Set solutions2Text = slide3.Shapes.AddTextbox(1, 510, 150, 380, 280)
    With solutions2Text.TextFrame.TextRange
        .Text = "✓ Comprehensive TCO analytics" & vbCrLf & "✓ ICE vs EV cost comparison" & vbCrLf & "✓ Data-driven expansion recommendations" & vbCrLf & "✓ Automated ESG reporting" & vbCrLf & "✓ Board-ready dashboards" & vbCrLf & vbCrLf & "SUCCESS METRICS:" & vbCrLf & "• 12% TCO reduction" & vbCrLf & "• 98% fleet uptime"
        .Font.Name = FONT_BODY
        .Font.Size = 16
        .Font.Color.RGB = COLOR_TEXT
        .Paragraphs(7).Font.Bold = True
        .Paragraphs(7).Font.Color.RGB = COLOR_PRIMARY
    End With
    
    ' Slide 4: Personas 3 & 4 (Combined)
    Dim slide4 As Object
    Set slide4 = AddSlide(12)
    
    Call AddSlideTitle(slide4, "Additional Key Personas")
    
    ' Persona 3: ESG Officer
    Call AddColorBox(slide4, MARGIN_LEFT, 100, 420, 50, RGB(76, 175, 80))
    Dim persona3Name As Object
    Set persona3Name = slide4.Shapes.AddTextbox(1, MARGIN_LEFT + 70, 110, 340, 30)
    With persona3Name.TextFrame.TextRange
        .Text = "Persona 3: Sustainability/ESG Officer"
        .Font.Name = FONT_BODY
        .Font.Size = 19
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
    End With
    
    Dim persona3Avatar As Object
    Set persona3Avatar = slide4.Shapes.AddShape(9, MARGIN_LEFT + 10, 105, 50, 50)
    persona3Avatar.Fill.ForeColor.RGB = RGB(255, 255, 255)
    persona3Avatar.Fill.Solid
    persona3Avatar.Line.Visible = False
    
    Dim persona3Details As Object
    Set persona3Details = slide4.Shapes.AddTextbox(1, MARGIN_LEFT + 10, 160, 400, 130)
    With persona3Details.TextFrame.TextRange
        .Text = "GOALS: Carbon footprint measurement, ESG compliance" & vbCrLf & vbCrLf & "PAIN POINTS: Manual data collection, lack of standardized metrics" & vbCrLf & vbCrLf & "OUR VALUE: Automated carbon accounting, GRI/CDP/BRSR templates, zero manual effort"
        .Font.Name = FONT_BODY
        .Font.Size = 14
        .Font.Color.RGB = COLOR_TEXT
    End With
    
    ' Persona 4: Finance Manager
    Call AddColorBox(slide4, 490, 100, 420, 50, RGB(255, 152, 0))
    Dim persona4Name As Object
    Set persona4Name = slide4.Shapes.AddTextbox(1, 560, 110, 340, 30)
    With persona4Name.TextFrame.TextRange
        .Text = "Persona 4: Finance Manager"
        .Font.Name = FONT_BODY
        .Font.Size = 19
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
    End With
    
    Dim persona4Avatar As Object
    Set persona4Avatar = slide4.Shapes.AddShape(9, 500, 105, 50, 50)
    persona4Avatar.Fill.ForeColor.RGB = RGB(255, 255, 255)
    persona4Avatar.Fill.Solid
    persona4Avatar.Line.Visible = False
    
    Dim persona4Details As Object
    Set persona4Details = slide4.Shapes.AddTextbox(1, 500, 160, 400, 130)
    With persona4Details.TextFrame.TextRange
        .Text = "GOALS: Reduce TCO, track ROI, manage vendor payments" & vbCrLf & vbCrLf & "PAIN POINTS: Opaque energy costs, difficult expense tracking" & vbCrLf & vbCrLf & "OUR VALUE: ERP integration, automated billing, 100% cost transparency, 10% savings"
        .Font.Name = FONT_BODY
        .Font.Size = 14
        .Font.Color.RGB = COLOR_TEXT
    End With
    
    ' Summary box
    Call AddColorBox(slide4, MARGIN_LEFT, 310, 860, 160, RGB(240, 240, 240))
    Dim summaryTitle As Object
    Set summaryTitle = slide4.Shapes.AddTextbox(1, MARGIN_LEFT + 20, 325, 820, 30)
    With summaryTitle.TextFrame.TextRange
        .Text = "ONE PLATFORM, MULTIPLE STAKEHOLDERS"
        .Font.Name = FONT_BODY
        .Font.Size = 24
        .Font.Bold = True
        .Font.Color.RGB = COLOR_PRIMARY
        .ParagraphFormat.Alignment = 2
    End With
    
    Dim summaryText As Object
    Set summaryText = slide4.Shapes.AddTextbox(1, MARGIN_LEFT + 40, 365, 820, 90)
    With summaryText.TextFrame.TextRange
        .Text = "Our platform serves the entire organization—from operations to finance to sustainability—" & _
                "with role-specific dashboards, automated workflows, and actionable insights. " & _
                "Each persona gets exactly what they need without data silos or manual work."
        .Font.Name = FONT_BODY
        .Font.Size = 16
        .Font.Color.RGB = COLOR_TEXT
        .ParagraphFormat.Alignment = 2
    End With
End Sub

' ====================================================================
' SECTION 4: SOLUTION OVERVIEW & CORE MODULES
' ====================================================================

Sub CreateSection4_SolutionOverview()
    Call ShowProgress("Section 4 - Solution Overview")
    
    ' Slide 1: Vision Statement
    Dim slide1 As Object
    Set slide1 = AddSlide(12)
    
    ' Set slide background color using Shape method
    Dim bgShape As Object
    Set bgShape = slide1.Shapes.AddShape(1, 0, 0, SLIDE_WIDTH, SLIDE_HEIGHT)
    With bgShape
        .Fill.ForeColor.RGB = RGB(0, 128, 96)
        .Fill.Solid
        .Line.Visible = False
    End With
    ' Send background shape to back
    slide1.Shapes(slide1.Shapes.Count).ZOrder 2
    
    Dim visionTitle As Object
    Set visionTitle = slide1.Shapes.AddTextbox(1, 100, 120, 760, 60)
    With visionTitle.TextFrame.TextRange
        .Text = "OUR VISION"
        .Font.Name = FONT_TITLE
        .Font.Size = 44
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
        .ParagraphFormat.Alignment = 2
    End With
    
    Dim visionStatement As Object
    Set visionStatement = slide1.Shapes.AddTextbox(1, 80, 200, 800, 120)
    With visionStatement.TextFrame.TextRange
        .Text = """Empowering India's logistics revolution with intelligent, sustainable, and scalable EV fleet management."""
        .Font.Name = FONT_BODY
        .Font.Size = 32
        .Font.Italic = True
        .Font.Color.RGB = RGB(220, 255, 245)
        .ParagraphFormat.Alignment = 2
    End With
    
    ' Key benefits
    Dim benefits As Object
    Set benefits = slide1.Shapes.AddTextbox(1, 150, 360, 660, 120)
    With benefits.TextFrame.TextRange
        .Text = "Operate Efficiently  |  Reduce Costs  |  Scale Confidently  |  Meet ESG Goals"
        .Font.Name = FONT_BODY
        .Font.Size = 20
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 200, 100)
        .ParagraphFormat.Alignment = 2
    End With
    
    ' Slide 2: Product Description
    Dim slide2 As Object
    Set slide2 = AddSlide(12)
    
    Call AddSlideTitle(slide2, "The Solution: Cloud-Native SaaS Platform")
    
    ' Main description box
    Call AddColorBox(slide2, MARGIN_LEFT, 100, 860, 120, RGB(232, 245, 233))
    Dim descTitle As Object
    Set descTitle = slide2.Shapes.AddTextbox(1, MARGIN_LEFT + 20, 110, 820, 30)
    With descTitle.TextFrame.TextRange
        .Text = "WHAT WE OFFER"
        .Font.Name = FONT_BODY
        .Font.Size = 22
        .Font.Bold = True
        .Font.Color.RGB = COLOR_PRIMARY
        .ParagraphFormat.Alignment = 2
    End With
    
    Dim descText As Object
    Set descText = slide2.Shapes.AddTextbox(1, MARGIN_LEFT + 30, 145, 800, 65)
    With descText.TextFrame.TextRange
        .Text = "A cloud-native, mobile-first SaaS platform providing end-to-end visibility and control over commercial electric vehicle fleets. Integrating real-time telematics, charging infrastructure management, predictive maintenance, and business intelligence into a unified system."
        .Font.Name = FONT_BODY
        .Font.Size = 17
        .Font.Color.RGB = COLOR_TEXT
        .ParagraphFormat.Alignment = 2
    End With
    
    ' Four key capabilities
    Call AddInfoCard(slide2, MARGIN_LEFT, 240, 200, 100, "Operate", "Real-time tracking" & vbCrLf & "Route optimization" & vbCrLf & "Charging orchestration", RGB(255, 243, 224))
    Call AddInfoCard(slide2, MARGIN_LEFT + 220, 240, 200, 100, "Reduce Costs", "Energy optimization" & vbCrLf & "Predictive maintenance" & vbCrLf & "Utilization analytics", RGB(227, 242, 253))
    Call AddInfoCard(slide2, MARGIN_LEFT + 440, 240, 200, 100, "Scale", "Data-driven insights" & vbCrLf & "TCO modeling" & vbCrLf & "Fleet expansion tools", RGB(248, 231, 248))
    Call AddInfoCard(slide2, MARGIN_LEFT + 660, 240, 200, 100, "ESG Goals", "Carbon accounting" & vbCrLf & "Automated reporting" & vbCrLf & "Compliance tracking", RGB(232, 245, 233))
    
    ' Platform characteristics
    Call AddColorBox(slide2, MARGIN_LEFT, 360, 860, 40, COLOR_SECONDARY)
    Dim charTitle As Object
    Set charTitle = slide2.Shapes.AddTextbox(1, MARGIN_LEFT + 20, 368, 820, 25)
    With charTitle.TextFrame.TextRange
        .Text = "PLATFORM CHARACTERISTICS"
        .Font.Name = FONT_BODY
        .Font.Size = 18
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
        .ParagraphFormat.Alignment = 2
    End With
    
    Dim chars(1 To 4) As String
    chars(1) = "☁ Cloud-Native: Scalable, secure, 99.9% uptime"
    chars(2) = "📱 Mobile-First: Progressive Web App + native apps"
    chars(3) = "🔌 API-First: Seamless integrations with OEMs, ERPs, charging networks"
    chars(4) = "🤖 AI-Powered: Machine learning for predictions and optimization"
    Call AddBulletList(slide2, MARGIN_LEFT + 40, 410, 820, 100, chars)
    
    ' Slide 3: Core Modules Overview
    Dim slide3 As Object
    Set slide3 = AddSlide(12)
    
    Call AddSlideTitle(slide3, "6 Core Modules: Comprehensive Coverage")
    
    ' Module 1: Fleet Command Center
    Call AddColorBox(slide3, MARGIN_LEFT, 100, 280, 50, RGB(63, 81, 181))
    Dim mod1Title As Object
    Set mod1Title = slide3.Shapes.AddTextbox(1, MARGIN_LEFT + 40, 108, 200, 35)
    With mod1Title.TextFrame.TextRange
        .Text = "1. Fleet Command Center"
        .Font.Name = FONT_BODY
        .Font.Size = 17
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
    End With
    Dim mod1Icon As Object
    Set mod1Icon = slide3.Shapes.AddShape(9, MARGIN_LEFT + 8, 108, 30, 30)
    mod1Icon.Fill.ForeColor.RGB = RGB(255, 255, 255)
    mod1Icon.Fill.Solid
    mod1Icon.Line.Visible = False
    
    Dim mod1Desc As Object
    Set mod1Desc = slide3.Shapes.AddTextbox(1, MARGIN_LEFT + 10, 158, 260, 90)
    With mod1Desc.TextFrame.TextRange
        .Text = "• Real-time fleet visibility" & vbCrLf & "• Vehicle locations & status" & vbCrLf & "• Battery levels & alerts" & vbCrLf & "• Driver status tracking"
        .Font.Name = FONT_BODY
        .Font.Size = 14
        .Font.Color.RGB = COLOR_TEXT
    End With
    
    ' Module 2: Charging Intelligence
    Call AddColorBox(slide3, 350, 100, 280, 50, RGB(0, 150, 136))
    Dim mod2Title As Object
    Set mod2Title = slide3.Shapes.AddTextbox(1, 390, 108, 230, 35)
    With mod2Title.TextFrame.TextRange
        .Text = "2. Charging Intelligence"
        .Font.Name = FONT_BODY
        .Font.Size = 17
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
    End With
    Dim mod2Icon As Object
    Set mod2Icon = slide3.Shapes.AddShape(9, 358, 108, 30, 30)
    mod2Icon.Fill.ForeColor.RGB = RGB(255, 255, 255)
    mod2Icon.Fill.Solid
    mod2Icon.Line.Visible = False
    
    Dim mod2Desc As Object
    Set mod2Desc = slide3.Shapes.AddTextbox(1, 360, 158, 260, 90)
    With mod2Desc.TextFrame.TextRange
        .Text = "• Multi-network integration" & vbCrLf & "• Smart charging orchestration" & vbCrLf & "• Cost optimization" & vbCrLf & "• Route-based recommendations"
        .Font.Name = FONT_BODY
        .Font.Size = 14
        .Font.Color.RGB = COLOR_TEXT
    End With
    
    ' Module 3: Vehicle Health & Maintenance
    Call AddColorBox(slide3, 650, 100, 280, 50, RGB(255, 87, 34))
    Dim mod3Title As Object
    Set mod3Title = slide3.Shapes.AddTextbox(1, 690, 108, 230, 35)
    With mod3Title.TextFrame.TextRange
        .Text = "3. Vehicle Health"
        .Font.Name = FONT_BODY
        .Font.Size = 17
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
    End With
    Dim mod3Icon As Object
    Set mod3Icon = slide3.Shapes.AddShape(9, 658, 108, 30, 30)
    mod3Icon.Fill.ForeColor.RGB = RGB(255, 255, 255)
    mod3Icon.Fill.Solid
    mod3Icon.Line.Visible = False
    
    Dim mod3Desc As Object
    Set mod3Desc = slide3.Shapes.AddTextbox(1, 660, 158, 260, 90)
    With mod3Desc.TextFrame.TextRange
        .Text = "• Predictive maintenance" & vbCrLf & "• Battery health analytics" & vbCrLf & "• Service scheduling" & vbCrLf & "• Warranty tracking"
        .Font.Name = FONT_BODY
        .Font.Size = 14
        .Font.Color.RGB = COLOR_TEXT
    End With
    
    ' Module 4: Analytics & BI
    Call AddColorBox(slide3, MARGIN_LEFT, 270, 280, 50, RGB(156, 39, 176))
    Dim mod4Title As Object
    Set mod4Title = slide3.Shapes.AddTextbox(1, MARGIN_LEFT + 40, 278, 230, 35)
    With mod4Title.TextFrame.TextRange
        .Text = "4. Analytics & BI"
        .Font.Name = FONT_BODY
        .Font.Size = 17
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
    End With
    Dim mod4Icon As Object
    Set mod4Icon = slide3.Shapes.AddShape(9, MARGIN_LEFT + 8, 278, 30, 30)
    mod4Icon.Fill.ForeColor.RGB = RGB(255, 255, 255)
    mod4Icon.Fill.Solid
    mod4Icon.Line.Visible = False
    
    Dim mod4Desc As Object
    Set mod4Desc = slide3.Shapes.AddTextbox(1, MARGIN_LEFT + 10, 328, 260, 90)
    With mod4Desc.TextFrame.TextRange
        .Text = "• Cost analytics & TCO" & vbCrLf & "• Utilization reports" & vbCrLf & "• Driver scorecards" & vbCrLf & "• Custom dashboards"
        .Font.Name = FONT_BODY
        .Font.Size = 14
        .Font.Color.RGB = COLOR_TEXT
    End With
    
    ' Module 5: Sustainability & Compliance
    Call AddColorBox(slide3, 350, 270, 280, 50, RGB(76, 175, 80))
    Dim mod5Title As Object
    Set mod5Title = slide3.Shapes.AddTextbox(1, 390, 278, 230, 35)
    With mod5Title.TextFrame.TextRange
        .Text = "5. Sustainability"
        .Font.Name = FONT_BODY
        .Font.Size = 17
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
    End With
    Dim mod5Icon As Object
    Set mod5Icon = slide3.Shapes.AddShape(9, 358, 278, 30, 30)
    mod5Icon.Fill.ForeColor.RGB = RGB(255, 255, 255)
    mod5Icon.Fill.Solid
    mod5Icon.Line.Visible = False
    
    Dim mod5Desc As Object
    Set mod5Desc = slide3.Shapes.AddTextbox(1, 360, 328, 260, 90)
    With mod5Desc.TextFrame.TextRange
        .Text = "• Carbon footprint tracking" & vbCrLf & "• ESG reporting (GRI/CDP/BRSR)" & vbCrLf & "• FAME subsidy docs" & vbCrLf & "• Compliance management"
        .Font.Name = FONT_BODY
        .Font.Size = 14
        .Font.Color.RGB = COLOR_TEXT
    End With
    
    ' Module 6: Driver & Operations
    Call AddColorBox(slide3, 650, 270, 280, 50, RGB(255, 152, 0))
    Dim mod6Title As Object
    Set mod6Title = slide3.Shapes.AddTextbox(1, 690, 278, 230, 35)
    With mod6Title.TextFrame.TextRange
        .Text = "6. Driver Management"
        .Font.Name = FONT_BODY
        .Font.Size = 17
        .Font.Bold = True
        .Font.Color.RGB = RGB(255, 255, 255)
    End With
    Dim mod6Icon As Object
    Set mod6Icon = slide3.Shapes.AddShape(9, 658, 278, 30, 30)
    mod6Icon.Fill.ForeColor.RGB = RGB(255, 255, 255)
    mod6Icon.Fill.Solid
    mod6Icon.Line.Visible = False
    
    Dim mod6Desc As Object
    Set mod6Desc = slide3.Shapes.AddTextbox(1, 660, 328, 260, 90)
    With mod6Desc.TextFrame.TextRange
        .Text = "• Driver assignment" & vbCrLf & "• Behavior monitoring" & vbCrLf & "• Performance incentives" & vbCrLf & "• Trip logs & attendance"
        .Font.Name = FONT_BODY
        .Font.Size = 14
        .Font.Color.RGB = COLOR_TEXT
    End With
    
    ' Bottom note
    Call AddColorBox(slide3, MARGIN_LEFT, 435, 860, 50, COLOR_LIGHT)
    Dim note As Object
    Set note = slide3.Shapes.AddTextbox(1, MARGIN_LEFT + 30, 445, 800, 30)
    With note.TextFrame.TextRange
        .Text = "All modules seamlessly integrated • Single source of truth • Role-based access"
        .Font.Name = FONT_BODY
        .Font.Size = 16
        .Font.Bold = True
        .Font.Color.RGB = COLOR_PRIMARY
        .ParagraphFormat.Alignment = 2
    End With
End Sub
